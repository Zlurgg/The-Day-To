package uk.co.zlurgg.thedayto.testutil

import uk.co.zlurgg.thedayto.core.data.util.toStorageEpoch
import uk.co.zlurgg.thedayto.core.domain.util.TimeProvider
import java.time.Instant
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.ZoneId

/**
 * Test fake for [TimeProvider].
 *
 * Provides controllable, deterministic time for tests.
 *
 * **NOT thread-safe** - intended for single-threaded test execution.
 * For parallel tests, create separate instances per test.
 *
 * @param fixedDate The date to return from [today]. Default: 2024-01-15
 * @param fixedTime The time component for [now]. Default: 10:30
 * @param fixedZone The timezone for [instant] conversion. Default: UTC
 */
class FakeTimeProvider(
    private var fixedDate: LocalDate = LocalDate.of(2024, 1, 15),
    private var fixedTime: LocalTime = LocalTime.of(10, 30),
    private var fixedZone: ZoneId = ZoneId.of("UTC"),
) : TimeProvider {

    override fun today(): LocalDate = fixedDate

    override fun now(): LocalDateTime = LocalDateTime.of(fixedDate, fixedTime)

    override fun instant(): Instant = now().atZone(fixedZone).toInstant()

    override fun todayStorageEpoch(): Long = fixedDate.toStorageEpoch()

    // ========== Test Helpers ==========

    /**
     * Advances the fixed date by the specified number of days.
     */
    fun advanceDays(days: Int) {
        fixedDate = fixedDate.plusDays(days.toLong())
    }

    /**
     * Sets the fixed date to a specific value.
     */
    fun setDate(date: LocalDate) {
        fixedDate = date
    }

    /**
     * Sets the fixed time component.
     */
    fun setTime(time: LocalTime) {
        fixedTime = time
    }

    /**
     * Sets the timezone for instant conversion.
     */
    fun setZone(zone: ZoneId) {
        fixedZone = zone
    }
}
