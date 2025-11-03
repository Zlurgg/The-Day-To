package uk.co.zlurgg.thedayto.core.domain.model

/**
 * Domain model for notification settings.
 *
 * Represents the user's notification preferences including
 * whether notifications are enabled and the scheduled time.
 *
 * @param enabled Whether daily notifications are enabled
 * @param hour Hour in 24-hour format (0-23)
 * @param minute Minute (0-59)
 */
data class NotificationSettings(
    val enabled: Boolean,
    val hour: Int,
    val minute: Int
)
