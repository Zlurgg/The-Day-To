package uk.co.zlurgg.thedayto.notification.data.scheduler

import org.junit.Assert.assertEquals
import org.junit.Test
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.ZoneOffset

/**
 * Unit tests for [NotificationSchedule] — the pure delay math behind daily notifications.
 *
 * The central guarantee under test is drift resistance: every delay is computed from the
 * fixed target time-of-day, so a late (e.g. Doze-deferred) fire still schedules the next
 * one at the exact target rather than "24h from the late run".
 */
class NotificationScheduleTest {

    private val utc: ZoneId = ZoneOffset.UTC

    private fun delay(now: LocalDateTime, hour: Int, minute: Int, zone: ZoneId = utc): Long =
        NotificationSchedule.initialDelaySeconds(now, zone, hour, minute)

    @Test
    fun `target later today returns delay until today's target`() {
        val now = LocalDateTime.of(2026, 7, 21, 8, 0, 0)
        // 08:00 -> 09:00 today = 1 hour
        assertEquals(3600L, delay(now, 9, 0))
    }

    @Test
    fun `target one minute away schedules today, not tomorrow`() {
        val now = LocalDateTime.of(2026, 7, 21, 8, 59, 0)
        assertEquals(60L, delay(now, 9, 0))
    }

    @Test
    fun `target already passed rolls to same time tomorrow`() {
        val now = LocalDateTime.of(2026, 7, 21, 10, 0, 0)
        // 10:00 -> 09:00 next day = 23 hours
        assertEquals(23L * 3600, delay(now, 9, 0))
    }

    @Test
    fun `target exactly now rolls to tomorrow`() {
        val now = LocalDateTime.of(2026, 7, 21, 9, 0, 0)
        assertEquals(24L * 3600, delay(now, 9, 0))
    }

    @Test
    fun `seconds and nanos on now do not shift the target`() {
        // now carries 30.5s; target is normalised to :00, so delay is 3600 - 30 = 3570.
        val now = LocalDateTime.of(2026, 7, 21, 8, 0, 30, 500_000_000)
        assertEquals(3570L, delay(now, 9, 0))
    }

    @Test
    fun `late fire re-anchors to next day's exact target (no drift)`() {
        // Worker ran 40 minutes late (09:40 for a 09:00 target).
        val now = LocalDateTime.of(2026, 7, 21, 9, 40, 0)
        // Next fire is tomorrow 09:00 = 23h20m, NOT 24h from the late run.
        assertEquals((23L * 3600) + (20L * 60), delay(now, 9, 0))

        // And the resulting scheduled instant is exactly 09:00 the next day.
        val scheduled = now.plusSeconds(delay(now, 9, 0))
        assertEquals(9, scheduled.hour)
        assertEquals(0, scheduled.minute)
        assertEquals(22, scheduled.dayOfMonth)
    }

    @Test
    fun `target time-of-day never drifts across many late fires`() {
        // Simulate the daily chain where every fire runs a few minutes late and that
        // lateness would accumulate under a periodic schedule. Here it must not.
        var current = LocalDateTime.of(2026, 7, 21, 9, 5, 0) // first fire, 5 min late
        repeat(60) {
            val scheduled = current.plusSeconds(delay(current, 9, 0))
            assertEquals("hour drifted on iteration $it", 9, scheduled.hour)
            assertEquals("minute drifted on iteration $it", 0, scheduled.minute)
            // Next day's worker also runs 7 minutes late.
            current = scheduled.plusMinutes(7)
        }
    }

    @Test
    fun `respects the provided timezone`() {
        val newYork = ZoneId.of("America/New_York")
        // 07:00 local -> 09:00 local, no DST transition on this date = 2 hours.
        val now = LocalDateTime.of(2026, 7, 21, 7, 0, 0)
        assertEquals(2L * 3600, delay(now, 9, 0, newYork))
    }
}
