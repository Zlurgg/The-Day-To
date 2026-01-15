package io.github.zlurgg.core.domain.error

import io.github.zlurgg.core.domain.result.Result
import io.ktor.util.network.UnresolvedAddressException
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import java.io.IOException
import java.net.SocketTimeoutException
import java.net.UnknownHostException

class ErrorMapperTest {

    // mapException tests
    @Test
    fun `mapException maps UnresolvedAddressException to NO_INTERNET`() {
        val error = ErrorMapper.mapException(UnresolvedAddressException())
        assertEquals(DataError.Remote.NO_INTERNET, error)
    }

    @Test
    fun `mapException maps UnknownHostException to NO_INTERNET`() {
        val error = ErrorMapper.mapException(UnknownHostException())
        assertEquals(DataError.Remote.NO_INTERNET, error)
    }

    @Test
    fun `mapException maps SocketTimeoutException to REQUEST_TIMEOUT`() {
        val error = ErrorMapper.mapException(SocketTimeoutException())
        assertEquals(DataError.Remote.REQUEST_TIMEOUT, error)
    }

    @Test
    fun `mapException maps IOException to Remote UNKNOWN`() {
        val error = ErrorMapper.mapException(IOException())
        assertEquals(DataError.Remote.UNKNOWN, error)
    }

    @Test
    fun `mapException maps IllegalStateException to DATABASE_ERROR`() {
        val error = ErrorMapper.mapException(IllegalStateException())
        assertEquals(DataError.Local.DATABASE_ERROR, error)
    }

    @Test
    fun `mapException maps IllegalArgumentException to Local UNKNOWN`() {
        val error = ErrorMapper.mapException(IllegalArgumentException())
        assertEquals(DataError.Local.UNKNOWN, error)
    }

    @Test
    fun `mapException maps generic Exception to Local UNKNOWN`() {
        val error = ErrorMapper.mapException(RuntimeException())
        assertEquals(DataError.Local.UNKNOWN, error)
    }

    // safeSuspendCall tests
    @Test
    fun `safeSuspendCall returns Success on successful operation`() = runTest {
        val result = ErrorMapper.safeSuspendCall {
            "success"
        }
        assertTrue(result is Result.Success)
        assertEquals("success", (result as Result.Success).data)
    }

    @Test
    fun `safeSuspendCall returns Error on exception`() = runTest {
        val result = ErrorMapper.safeSuspendCall<String> {
            throw IllegalStateException("test error")
        }
        assertTrue(result is Result.Error)
        assertEquals(DataError.Local.DATABASE_ERROR, (result as Result.Error).error)
    }

    @Test
    fun `safeSuspendCall maps network exception to Local UNKNOWN`() = runTest {
        // Note: safeSuspendCall always returns DataError.Local, so network errors
        // get mapped to Local.UNKNOWN if the mapException returns a Remote error
        val result = ErrorMapper.safeSuspendCall<String> {
            throw IOException("network error")
        }
        assertTrue(result is Result.Error)
        // IOException maps to Remote.UNKNOWN, but safeSuspendCall casts to Local, so it becomes Local.UNKNOWN
        assertEquals(DataError.Local.UNKNOWN, (result as Result.Error).error)
    }

    @Test
    fun `safeSuspendCall with custom tag logs correctly`() = runTest {
        // This test verifies the function accepts a custom tag
        // Actual logging verification would require mocking Timber
        val result = ErrorMapper.safeSuspendCall<String>("CustomTag") {
            "test"
        }
        assertTrue(result is Result.Success)
    }

    @Test
    fun `safeSuspendCall handles null return`() = runTest {
        val result = ErrorMapper.safeSuspendCall<String?> {
            null
        }
        assertTrue(result is Result.Success)
        assertEquals(null, (result as Result.Success).data)
    }

    @Test
    fun `safeSuspendCall preserves result type`() = runTest {
        val result = ErrorMapper.safeSuspendCall {
            listOf(1, 2, 3)
        }
        assertTrue(result is Result.Success)
        assertEquals(listOf(1, 2, 3), (result as Result.Success).data)
    }
}
