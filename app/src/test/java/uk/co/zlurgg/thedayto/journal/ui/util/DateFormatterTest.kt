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
    fun `formatDate returns formatted string`() {
        val epoch = LocalDate.of(2024, 1, 5).toStorageEpoch()
        val formatted = DateFormatter.formatDate(epoch)

        // Check contains expected parts (locale-independent)
        assertEquals(true, formatted.contains("05"))
        assertEquals(true, formatted.contains("2024"))
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
