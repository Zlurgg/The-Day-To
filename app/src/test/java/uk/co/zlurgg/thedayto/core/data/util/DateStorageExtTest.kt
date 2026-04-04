package uk.co.zlurgg.thedayto.core.data.util

import org.junit.Assert.assertEquals
import org.junit.Test
import java.time.LocalDate

/**
 * Tests for DateStorageExt pure conversion functions.
 *
 * These are deterministic mathematical conversions - no mocking needed.
 */
class DateStorageExtTest {

    @Test
    fun `toStorageEpoch converts LocalDate to UTC midnight epoch`() {
        // 2024-01-01 00:00:00 UTC = 1704067200
        val date = LocalDate.of(2024, 1, 1)
        assertEquals(1704067200L, date.toStorageEpoch())
    }

    @Test
    fun `toStorageEpoch handles different dates correctly`() {
        // 2024-06-15 00:00:00 UTC = 1718409600
        val date = LocalDate.of(2024, 6, 15)
        assertEquals(1718409600L, date.toStorageEpoch())
    }

    @Test
    fun `toLocalDate converts epoch back to LocalDate`() {
        assertEquals(
            LocalDate.of(2024, 1, 1),
            1704067200L.toLocalDate()
        )
    }

    @Test
    fun `toLocalDate handles different epochs correctly`() {
        assertEquals(
            LocalDate.of(2024, 6, 15),
            1718409600L.toLocalDate()
        )
    }

    @Test
    fun `roundtrip preserves date`() {
        val original = LocalDate.of(2024, 6, 15)
        val epoch = original.toStorageEpoch()
        val restored = epoch.toLocalDate()
        assertEquals(original, restored)
    }

    @Test
    fun `roundtrip works for edge dates`() {
        // Test year boundaries
        listOf(
            LocalDate.of(2024, 1, 1), // New Year
            LocalDate.of(2024, 12, 31), // Year End
            LocalDate.of(2000, 1, 1), // Y2K
            LocalDate.of(2038, 1, 19), // Near Unix 32-bit limit
        ).forEach { date ->
            assertEquals(
                "Roundtrip failed for $date",
                date,
                date.toStorageEpoch().toLocalDate()
            )
        }
    }

    @Test
    fun `epoch zero represents Unix epoch date`() {
        // Epoch 0 = 1970-01-01
        assertEquals(LocalDate.of(1970, 1, 1), 0L.toLocalDate())
    }
}
