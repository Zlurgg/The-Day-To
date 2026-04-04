package uk.co.zlurgg.thedayto.journal.ui.util

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import uk.co.zlurgg.thedayto.core.data.util.toStorageEpoch
import uk.co.zlurgg.thedayto.testutil.FakeTimeProvider
import java.time.LocalDate

/**
 * Tests for CalendarHelper date comparison utilities.
 */
class CalendarHelperTest {

    private lateinit var fakeTimeProvider: FakeTimeProvider
    private lateinit var calendarHelper: CalendarHelper

    @Before
    fun setup() {
        fakeTimeProvider = FakeTimeProvider(fixedDate = LocalDate.of(2024, 6, 15))
        calendarHelper = CalendarHelper(fakeTimeProvider)
    }

    // ============================================================
    // isToday Tests
    // ============================================================

    @Test
    fun `isToday returns true for today's epoch`() {
        val todayEpoch = LocalDate.of(2024, 6, 15).toStorageEpoch()
        assertTrue(calendarHelper.isToday(todayEpoch))
    }

    @Test
    fun `isToday returns false for yesterday`() {
        val yesterdayEpoch = LocalDate.of(2024, 6, 14).toStorageEpoch()
        assertFalse(calendarHelper.isToday(yesterdayEpoch))
    }

    @Test
    fun `isToday returns false for tomorrow`() {
        val tomorrowEpoch = LocalDate.of(2024, 6, 16).toStorageEpoch()
        assertFalse(calendarHelper.isToday(tomorrowEpoch))
    }

    // ============================================================
    // isPast Tests
    // ============================================================

    @Test
    fun `isPast returns true for yesterday`() {
        val yesterdayEpoch = LocalDate.of(2024, 6, 14).toStorageEpoch()
        assertTrue(calendarHelper.isPast(yesterdayEpoch))
    }

    @Test
    fun `isPast returns false for today`() {
        val todayEpoch = LocalDate.of(2024, 6, 15).toStorageEpoch()
        assertFalse(calendarHelper.isPast(todayEpoch))
    }

    @Test
    fun `isPast returns false for tomorrow`() {
        val tomorrowEpoch = LocalDate.of(2024, 6, 16).toStorageEpoch()
        assertFalse(calendarHelper.isPast(tomorrowEpoch))
    }

    // ============================================================
    // isFuture Tests
    // ============================================================

    @Test
    fun `isFuture returns true for tomorrow`() {
        val tomorrowEpoch = LocalDate.of(2024, 6, 16).toStorageEpoch()
        assertTrue(calendarHelper.isFuture(tomorrowEpoch))
    }

    @Test
    fun `isFuture returns false for today`() {
        val todayEpoch = LocalDate.of(2024, 6, 15).toStorageEpoch()
        assertFalse(calendarHelper.isFuture(todayEpoch))
    }

    @Test
    fun `isFuture returns false for yesterday`() {
        val yesterdayEpoch = LocalDate.of(2024, 6, 14).toStorageEpoch()
        assertFalse(calendarHelper.isFuture(yesterdayEpoch))
    }

    // ============================================================
    // isInMonth Tests
    // ============================================================

    @Test
    fun `isInMonth returns true for matching month and year`() {
        val epoch = LocalDate.of(2024, 6, 15).toStorageEpoch()
        assertTrue(calendarHelper.isInMonth(epoch, 2024, 6))
    }

    @Test
    fun `isInMonth returns false for different month`() {
        val epoch = LocalDate.of(2024, 6, 15).toStorageEpoch()
        assertFalse(calendarHelper.isInMonth(epoch, 2024, 7))
    }

    @Test
    fun `isInMonth returns false for different year`() {
        val epoch = LocalDate.of(2024, 6, 15).toStorageEpoch()
        assertFalse(calendarHelper.isInMonth(epoch, 2023, 6))
    }

    // ============================================================
    // calculateMonthsBetween Tests
    // ============================================================

    @Test
    fun `calculateMonthsBetween returns correct positive months`() {
        val start = LocalDate.of(2024, 1, 1)
        val end = LocalDate.of(2024, 6, 1)
        assertEquals(5, calendarHelper.calculateMonthsBetween(start, end))
    }

    @Test
    fun `calculateMonthsBetween returns zero for same month`() {
        val start = LocalDate.of(2024, 6, 1)
        val end = LocalDate.of(2024, 6, 15)
        assertEquals(0, calendarHelper.calculateMonthsBetween(start, end))
    }

    @Test
    fun `calculateMonthsBetween returns negative for reversed dates`() {
        val start = LocalDate.of(2024, 6, 1)
        val end = LocalDate.of(2024, 1, 1)
        assertEquals(-5, calendarHelper.calculateMonthsBetween(start, end))
    }

    // ============================================================
    // dayToStorageEpoch Tests
    // ============================================================

    @Test
    fun `dayToStorageEpoch converts correctly`() {
        val expected = LocalDate.of(2024, 6, 15).toStorageEpoch()
        assertEquals(expected, calendarHelper.dayToStorageEpoch(15, 6, 2024))
    }

    // ============================================================
    // TimeProvider Integration Tests
    // ============================================================

    @Test
    fun `isToday updates when time provider changes`() {
        val june15Epoch = LocalDate.of(2024, 6, 15).toStorageEpoch()
        val june16Epoch = LocalDate.of(2024, 6, 16).toStorageEpoch()

        // Initially June 15 is today
        assertTrue(calendarHelper.isToday(june15Epoch))
        assertFalse(calendarHelper.isToday(june16Epoch))

        // Advance to June 16
        fakeTimeProvider.advanceDays(1)

        // Now June 16 is today
        assertFalse(calendarHelper.isToday(june15Epoch))
        assertTrue(calendarHelper.isToday(june16Epoch))
    }
}
