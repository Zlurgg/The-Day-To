package uk.co.zlurgg.thedayto.notification.data.scheduler

import java.time.LocalDateTime
import java.time.ZoneId

/**
 * Pure time math for daily notification scheduling.
 *
 * Deliberately free of Android/WorkManager dependencies so the drift behaviour is
 * unit-testable in isolation.
 */
internal object NotificationSchedule {

    /**
     * Seconds from [now] until the next occurrence of [hour]:[minute] in [zone].
     *
     * The result is always anchored to the fixed target time-of-day: if the target has
     * already passed today (or is exactly [now]), it rolls forward to the same time
     * tomorrow. Because every schedule is computed from the fixed target rather than
     * "24h after the last run", the notification cannot drift later day over day.
     *
     * @param now current local date-time (user's timezone)
     * @param zone zone used to resolve wall-clock time to an absolute instant
     * @param hour target hour (0-23)
     * @param minute target minute (0-59)
     * @return delay in seconds until the next target time (always > 0)
     */
    fun initialDelaySeconds(now: LocalDateTime, zone: ZoneId, hour: Int, minute: Int): Long {
        var next = now
            .withHour(hour)
            .withMinute(minute)
            .withSecond(0)
            .withNano(0)

        if (!next.isAfter(now)) {
            next = next.plusDays(1)
        }

        val nowEpoch = now.atZone(zone).toEpochSecond()
        val nextEpoch = next.atZone(zone).toEpochSecond()
        return nextEpoch - nowEpoch
    }
}
