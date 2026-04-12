package uk.co.zlurgg.thedayto.journal.ui.util

import org.junit.Assert.assertEquals
import org.junit.Test
import uk.co.zlurgg.thedayto.core.data.util.toStorageEpoch
import java.time.LocalDate

/**
 * Tests for DateFormatter stateless formatting utilities.
 */
class DateFormatterTest {

    @Test
    fun `formatDay extracts day of month`() {
        val epoch = LocalDate.of(2024, 6, 15).toStorageEpoch()
        assertEquals(15, DateFormatter.formatDay(epoch))
    }

    @Test
    fun `formatMonthValue extracts month number`() {
        val epoch = LocalDate.of(2024, 6, 15).toStorageEpoch()
        assertEquals(6, DateFormatter.formatMonthValue(epoch))
    }

    @Test
    fun `formatYear extracts year`() {
        val epoch = LocalDate.of(2024, 6, 15).toStorageEpoch()
        assertEquals(2024, DateFormatter.formatYear(epoch))
    }

    @Test
    fun `formatDateCompact returns ordinal date with short month`() {
        val epoch = LocalDate.of(2024, 1, 5).toStorageEpoch()
        val formatted = DateFormatter.formatDateCompact(epoch)

        // Check contains ordinal day + year (locale-independent)
        assertEquals(true, formatted.contains("5th"))
        assertEquals(true, formatted.contains("2024"))
    }

    @Test
    fun `formatDateOrdinal returns correct ordinal suffixes`() {
        fun suffix(day: Int): String {
            val epoch = LocalDate.of(2024, 1, day).toStorageEpoch()
            return DateFormatter.formatDateOrdinal(epoch).suffix
        }
        assertEquals("st", suffix(1))
        assertEquals("nd", suffix(2))
        assertEquals("rd", suffix(3))
        assertEquals("th", suffix(4))
        assertEquals("th", suffix(11))
        assertEquals("th", suffix(12))
        assertEquals("th", suffix(13))
        assertEquals("st", suffix(21))
        assertEquals("nd", suffix(22))
        assertEquals("rd", suffix(23))
        assertEquals("st", suffix(31))
    }

    @Test
    fun `formatMonthYear returns full month and year`() {
        val date = LocalDate.of(2024, 1, 15)
        val formatted = DateFormatter.formatMonthYear(date)
        assertEquals(true, formatted.contains("2024"))
        // Full month name (locale-dependent, but "January" in English)
        assertEquals(true, formatted.length > 8) // "X 2024" minimum
    }

    @Test
    fun `formatDay handles single digit days`() {
        val epoch = LocalDate.of(2024, 1, 1).toStorageEpoch()
        assertEquals(1, DateFormatter.formatDay(epoch))
    }

    @Test
    fun `formatDay handles end of month`() {
        val epoch = LocalDate.of(2024, 1, 31).toStorageEpoch()
        assertEquals(31, DateFormatter.formatDay(epoch))
    }

    @Test
    fun `formatMonthValue handles all months`() {
        for (month in 1..12) {
            val epoch = LocalDate.of(2024, month, 15).toStorageEpoch()
            assertEquals(month, DateFormatter.formatMonthValue(epoch))
        }
    }
}
