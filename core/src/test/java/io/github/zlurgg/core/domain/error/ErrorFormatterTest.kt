package io.github.zlurgg.core.domain.error

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class ErrorFormatterTest {

    // Remote errors
    @Test
    fun `format Remote REQUEST_TIMEOUT`() {
        val message = ErrorFormatter.format(DataError.Remote.REQUEST_TIMEOUT)
        assertEquals("Request timed out. Please try again.", message)
    }

    @Test
    fun `format Remote NO_INTERNET`() {
        val message = ErrorFormatter.format(DataError.Remote.NO_INTERNET)
        assertEquals("No internet connection.", message)
    }

    @Test
    fun `format Remote SERVER_ERROR`() {
        val message = ErrorFormatter.format(DataError.Remote.SERVER_ERROR)
        assertEquals("Server error. Please try again later.", message)
    }

    @Test
    fun `format Remote NOT_FOUND`() {
        val message = ErrorFormatter.format(DataError.Remote.NOT_FOUND)
        assertEquals("Not found.", message)
    }

    @Test
    fun `format Remote UNKNOWN`() {
        val message = ErrorFormatter.format(DataError.Remote.UNKNOWN)
        assertEquals("Network error occurred.", message)
    }

    // Local errors
    @Test
    fun `format Local DATABASE_ERROR`() {
        val message = ErrorFormatter.format(DataError.Local.DATABASE_ERROR)
        assertEquals("Database error occurred.", message)
    }

    @Test
    fun `format Local NOT_FOUND`() {
        val message = ErrorFormatter.format(DataError.Local.NOT_FOUND)
        assertEquals("Item not found.", message)
    }

    @Test
    fun `format Local DUPLICATE_ENTRY`() {
        val message = ErrorFormatter.format(DataError.Local.DUPLICATE_ENTRY)
        assertEquals("Entry already exists for this date.", message)
    }

    @Test
    fun `format Local UNKNOWN`() {
        val message = ErrorFormatter.format(DataError.Local.UNKNOWN)
        assertEquals("An error occurred.", message)
    }

    // Validation errors
    @Test
    fun `format Validation EMPTY_MOOD`() {
        val message = ErrorFormatter.format(DataError.Validation.EMPTY_MOOD)
        assertEquals("Please select a mood.", message)
    }

    @Test
    fun `format Validation EMPTY_COLOR`() {
        val message = ErrorFormatter.format(DataError.Validation.EMPTY_COLOR)
        assertEquals("Please select a color.", message)
    }

    @Test
    fun `format Validation CONTENT_TOO_LONG`() {
        val message = ErrorFormatter.format(DataError.Validation.CONTENT_TOO_LONG)
        assertEquals("Note is too long.", message)
    }

    @Test
    fun `format Validation INVALID_DATE`() {
        val message = ErrorFormatter.format(DataError.Validation.INVALID_DATE)
        assertEquals("Invalid date selected.", message)
    }

    @Test
    fun `format Validation MOOD_NAME_EXISTS`() {
        val message = ErrorFormatter.format(DataError.Validation.MOOD_NAME_EXISTS)
        assertEquals("A mood with this name already exists.", message)
    }

    @Test
    fun `format Validation MOOD_NOT_FOUND`() {
        val message = ErrorFormatter.format(DataError.Validation.MOOD_NOT_FOUND)
        assertEquals("Selected mood no longer exists.", message)
    }

    @Test
    fun `format Validation MOOD_DELETED`() {
        val message = ErrorFormatter.format(DataError.Validation.MOOD_DELETED)
        assertEquals("Selected mood has been deleted.", message)
    }

    // Auth errors
    @Test
    fun `format Auth CANCELLED`() {
        val message = ErrorFormatter.format(DataError.Auth.CANCELLED)
        assertEquals("Sign-in was cancelled.", message)
    }

    @Test
    fun `format Auth NO_CREDENTIAL`() {
        val message = ErrorFormatter.format(DataError.Auth.NO_CREDENTIAL)
        assertEquals("No Google account found on device.", message)
    }

    @Test
    fun `format Auth FAILED`() {
        val message = ErrorFormatter.format(DataError.Auth.FAILED)
        assertEquals("Sign-in failed. Please try again.", message)
    }

    @Test
    fun `format Auth NETWORK_ERROR`() {
        val message = ErrorFormatter.format(DataError.Auth.NETWORK_ERROR)
        assertEquals("Network error during sign-in.", message)
    }

    // With operation context
    @Test
    fun `format with operation adds context`() {
        val message = ErrorFormatter.format(DataError.Local.NOT_FOUND, "load entry")
        assertEquals("Failed to load entry: Item not found.", message)
    }

    @Test
    fun `format with blank operation omits context`() {
        val message = ErrorFormatter.format(DataError.Local.NOT_FOUND, "")
        assertEquals("Item not found.", message)
    }

    @Test
    fun `format with whitespace operation omits context`() {
        val message = ErrorFormatter.format(DataError.Local.NOT_FOUND, "   ")
        assertEquals("Item not found.", message)
    }

    // Exhaustive coverage check
    @Test
    fun `all Remote errors are covered`() {
        DataError.Remote.entries.forEach { error ->
            val message = ErrorFormatter.format(error)
            assertTrue("Message should not be empty for $error", message.isNotBlank())
        }
    }

    @Test
    fun `all Local errors are covered`() {
        DataError.Local.entries.forEach { error ->
            val message = ErrorFormatter.format(error)
            assertTrue("Message should not be empty for $error", message.isNotBlank())
        }
    }

    @Test
    fun `all Validation errors are covered`() {
        DataError.Validation.entries.forEach { error ->
            val message = ErrorFormatter.format(error)
            assertTrue("Message should not be empty for $error", message.isNotBlank())
        }
    }

    @Test
    fun `all Auth errors are covered`() {
        DataError.Auth.entries.forEach { error ->
            val message = ErrorFormatter.format(error)
            assertTrue("Message should not be empty for $error", message.isNotBlank())
        }
    }
}
