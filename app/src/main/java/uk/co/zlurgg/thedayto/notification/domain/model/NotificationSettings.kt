package uk.co.zlurgg.thedayto.notification.domain.model

/**
 * Domain model for notification settings.
 *
 * Validates hour/minute on construction to ensure new settings created in-app
 * are always valid. For data loaded from storage, use [NotificationSettingsEntity.toDomain]
 * which returns null for corrupt data instead of crashing.
 *
 * @param enabled Whether daily notifications are enabled
 * @param hour Hour in 24-hour format (0-23)
 * @param minute Minute (0-59)
 */
data class NotificationSettings(
    val enabled: Boolean = false,
    val hour: Int = DEFAULT_HOUR,
    val minute: Int = DEFAULT_MINUTE
) {
    init {
        require(hour in 0..MAX_HOUR) { "Hour must be 0-$MAX_HOUR, was $hour" }
        require(minute in 0..MAX_MINUTE) { "Minute must be 0-$MAX_MINUTE, was $minute" }
    }

    companion object {
        const val DEFAULT_HOUR = 9
        const val DEFAULT_MINUTE = 0
        const val MAX_HOUR = 23
        const val MAX_MINUTE = 59
    }
}
