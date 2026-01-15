package io.github.zlurgg.core.domain.result

import io.github.zlurgg.core.domain.error.DataError
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class ResultTest {

    @Test
    fun `Success contains data`() {
        val result: Result<String, DataError> = Result.Success("test")
        assertTrue(result is Result.Success)
        assertEquals("test", (result as Result.Success).data)
    }

    @Test
    fun `Error contains error`() {
        val result: Result<String, DataError.Local> = Result.Error(DataError.Local.NOT_FOUND)
        assertTrue(result is Result.Error)
        assertEquals(DataError.Local.NOT_FOUND, (result as Result.Error).error)
    }

    @Test
    fun `map transforms success value`() {
        val result: Result<Int, DataError> = Result.Success(5)
        val mapped = result.map { it * 2 }
        assertEquals(10, (mapped as Result.Success).data)
    }

    @Test
    fun `map preserves error`() {
        val result: Result<Int, DataError.Local> = Result.Error(DataError.Local.NOT_FOUND)
        val mapped = result.map { it * 2 }
        assertTrue(mapped is Result.Error)
        assertEquals(DataError.Local.NOT_FOUND, (mapped as Result.Error).error)
    }

    @Test
    fun `flatMap chains success operations`() {
        val result: Result<Int, DataError.Local> = Result.Success(5)
        val chained = result.flatMap { Result.Success(it.toString()) }
        assertEquals("5", (chained as Result.Success).data)
    }

    @Test
    fun `flatMap short-circuits on error`() {
        val result: Result<Int, DataError.Local> = Result.Error(DataError.Local.NOT_FOUND)
        val chained = result.flatMap { Result.Success(it.toString()) }
        assertTrue(chained is Result.Error)
    }

    @Test
    fun `onSuccess executes action for success`() {
        var executed = false
        val result: Result<String, DataError> = Result.Success("test")
        result.onSuccess { executed = true }
        assertTrue(executed)
    }

    @Test
    fun `onSuccess does not execute for error`() {
        var executed = false
        val result: Result<String, DataError.Local> = Result.Error(DataError.Local.NOT_FOUND)
        result.onSuccess { executed = true }
        assertTrue(!executed)
    }

    @Test
    fun `onError executes action for error`() {
        var executed = false
        val result: Result<String, DataError.Local> = Result.Error(DataError.Local.NOT_FOUND)
        result.onError { executed = true }
        assertTrue(executed)
    }

    @Test
    fun `onError does not execute for success`() {
        var executed = false
        val result: Result<String, DataError> = Result.Success("test")
        result.onError { executed = true }
        assertTrue(!executed)
    }

    @Test
    fun `getOrNull returns data for success`() {
        val result: Result<String, DataError> = Result.Success("test")
        assertEquals("test", result.getOrNull())
    }

    @Test
    fun `getOrNull returns null for error`() {
        val result: Result<String, DataError.Local> = Result.Error(DataError.Local.NOT_FOUND)
        assertNull(result.getOrNull())
    }

    @Test
    fun `getOrDefault returns data for success`() {
        val result: Result<String, DataError> = Result.Success("test")
        assertEquals("test", result.getOrDefault("default"))
    }

    @Test
    fun `getOrDefault returns default for error`() {
        val result: Result<String, DataError.Local> = Result.Error(DataError.Local.NOT_FOUND)
        assertEquals("default", result.getOrDefault("default"))
    }

    @Test
    fun `getOrElse returns data for success`() {
        val result: Result<String, DataError> = Result.Success("test")
        assertEquals("test", result.getOrElse { "fallback" })
    }

    @Test
    fun `getOrElse computes value for error`() {
        val result: Result<String, DataError.Local> = Result.Error(DataError.Local.NOT_FOUND)
        assertEquals("not found", result.getOrElse { "not found" })
    }

    @Test
    fun `fold applies onSuccess for success`() {
        val result: Result<Int, DataError> = Result.Success(5)
        val folded = result.fold(
            onSuccess = { "success: $it" },
            onError = { "error" }
        )
        assertEquals("success: 5", folded)
    }

    @Test
    fun `fold applies onError for error`() {
        val result: Result<Int, DataError.Local> = Result.Error(DataError.Local.NOT_FOUND)
        val folded = result.fold(
            onSuccess = { "success" },
            onError = { "error: $it" }
        )
        assertEquals("error: NOT_FOUND", folded)
    }

    @Test
    fun `asEmptyResult discards success value`() {
        val result: Result<String, DataError> = Result.Success("test")
        val empty = result.asEmptyResult()
        assertTrue(empty is Result.Success)
        assertEquals(Unit, (empty as Result.Success).data)
    }

    @Test
    fun `asEmptyResult preserves error`() {
        val result: Result<String, DataError.Local> = Result.Error(DataError.Local.NOT_FOUND)
        val empty = result.asEmptyResult()
        assertTrue(empty is Result.Error)
    }
}
