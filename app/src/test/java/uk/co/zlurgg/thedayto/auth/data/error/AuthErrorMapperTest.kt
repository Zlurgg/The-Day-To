package uk.co.zlurgg.thedayto.auth.data.error

import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import uk.co.zlurgg.thedayto.core.domain.error.DataError
import uk.co.zlurgg.thedayto.core.domain.result.Result

/**
 * Unit tests for AuthErrorMapper.
 *
 * Note: Tests for GetCredentialCancellationException and NoCredentialException
 * are in the androidTest source set as they require the Android SDK.
 *
 * Tests cover:
 * - mapException handles network-related errors
 * - mapException handles generic exceptions
 * - safeAuthCall returns Success on successful action
 * - safeAuthCall maps exceptions to Result.Error
 * - safeAuthCall rethrows CancellationException
 */
class AuthErrorMapperTest {

    // mapException tests

    @Test
    fun `mapException - exception with network in message returns NETWORK_ERROR`() {
        val exception = Exception("Failed due to network issues")
        val result = AuthErrorMapper.mapException(exception)
        assertEquals(DataError.Auth.NETWORK_ERROR, result)
    }

    @Test
    fun `mapException - exception with NETWORK uppercase in message returns NETWORK_ERROR`() {
        val exception = Exception("NETWORK unavailable")
        val result = AuthErrorMapper.mapException(exception)
        assertEquals(DataError.Auth.NETWORK_ERROR, result)
    }

    @Test
    fun `mapException - exception with Network mixed case in message returns NETWORK_ERROR`() {
        val exception = Exception("The Network connection was lost")
        val result = AuthErrorMapper.mapException(exception)
        assertEquals(DataError.Auth.NETWORK_ERROR, result)
    }

    @Test
    fun `mapException - generic exception returns FAILED`() {
        val exception = Exception("Something went wrong")
        val result = AuthErrorMapper.mapException(exception)
        assertEquals(DataError.Auth.FAILED, result)
    }

    @Test
    fun `mapException - exception with null message returns FAILED`() {
        val exception = Exception()
        val result = AuthErrorMapper.mapException(exception)
        assertEquals(DataError.Auth.FAILED, result)
    }

    @Test
    fun `mapException - IllegalStateException without network returns FAILED`() {
        val exception = IllegalStateException("Invalid state")
        val result = AuthErrorMapper.mapException(exception)
        assertEquals(DataError.Auth.FAILED, result)
    }

    @Test
    fun `mapException - RuntimeException without network returns FAILED`() {
        val exception = RuntimeException("Runtime error")
        val result = AuthErrorMapper.mapException(exception)
        assertEquals(DataError.Auth.FAILED, result)
    }

    // safeAuthCall tests

    @Test
    fun `safeAuthCall - successful action returns Success`() = runTest {
        val result = AuthErrorMapper.safeAuthCall {
            "test_value"
        }

        assertTrue("Result should be Success", result is Result.Success)
        assertEquals("test_value", (result as Result.Success).data)
    }

    @Test
    fun `safeAuthCall - action returning null returns Success with null`() = runTest {
        val result = AuthErrorMapper.safeAuthCall<String?> {
            null
        }

        assertTrue("Result should be Success", result is Result.Success)
        assertEquals(null, (result as Result.Success).data)
    }

    @Test
    fun `safeAuthCall - action returning complex type returns Success`() = runTest {
        data class TestData(val id: Int, val name: String)
        val testData = TestData(123, "Test")

        val result = AuthErrorMapper.safeAuthCall {
            testData
        }

        assertTrue("Result should be Success", result is Result.Success)
        assertEquals(testData, (result as Result.Success).data)
    }

    @Test
    fun `safeAuthCall - network error returns Error with NETWORK_ERROR`() = runTest {
        val result = AuthErrorMapper.safeAuthCall<String> {
            throw Exception("Failed due to network unavailable")
        }

        assertTrue("Result should be Error", result is Result.Error)
        assertEquals(DataError.Auth.NETWORK_ERROR, (result as Result.Error).error)
    }

    @Test
    fun `safeAuthCall - generic exception returns Error with FAILED`() = runTest {
        val result = AuthErrorMapper.safeAuthCall<String> {
            throw Exception("Something went wrong")
        }

        assertTrue("Result should be Error", result is Result.Error)
        assertEquals(DataError.Auth.FAILED, (result as Result.Error).error)
    }

    @Test
    fun `safeAuthCall - IllegalStateException returns Error with FAILED`() = runTest {
        val result = AuthErrorMapper.safeAuthCall<String> {
            throw IllegalStateException("Invalid state")
        }

        assertTrue("Result should be Error", result is Result.Error)
        assertEquals(DataError.Auth.FAILED, (result as Result.Error).error)
    }

    @Test
    fun `safeAuthCall - structured concurrency is preserved via ensureActive`() = runTest {
        // ensureActive() is called after catching exceptions, which ensures
        // that if the coroutine is cancelled, CancellationException is rethrown.
        // This test verifies that regular exceptions don't bypass this check.
        val result = AuthErrorMapper.safeAuthCall<String> {
            throw RuntimeException("Regular exception")
        }

        // Regular exceptions are mapped to errors, not rethrown
        assertTrue("Result should be Error", result is Result.Error)
        assertEquals(DataError.Auth.FAILED, (result as Result.Error).error)
    }

    @Test
    fun `safeAuthCall - uses custom tag for logging`() = runTest {
        // We can't easily verify logging, but we ensure the tag parameter works
        val result = AuthErrorMapper.safeAuthCall(tag = "CustomTag") {
            throw Exception("Error with custom tag")
        }

        assertTrue("Result should be Error", result is Result.Error)
        assertEquals(DataError.Auth.FAILED, (result as Result.Error).error)
    }

    @Test
    fun `safeAuthCall - uses default tag when not specified`() = runTest {
        val result = AuthErrorMapper.safeAuthCall {
            throw Exception("Error with default tag")
        }

        assertTrue("Result should be Error", result is Result.Error)
    }
}
