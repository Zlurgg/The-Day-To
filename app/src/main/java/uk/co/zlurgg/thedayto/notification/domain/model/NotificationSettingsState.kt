package uk.co.zlurgg.thedayto.notification.domain.model

/**
 * Sealed interface representing the state of notification settings for a user.
 *
 * This provides clear distinction between:
 * - User has never configured notifications ([NotConfigured])
 * - User has configured notifications, which may be enabled or disabled ([Configured])
 *
 * Use this for UI state to show appropriate screens:
 * - NotConfigured → show onboarding/setup
 * - Configured + enabled → show enabled state with time
 * - Configured + disabled → show disabled state with option to enable
 */
sealed interface NotificationSettingsState {

    /**
     * No notification settings exist for this user.
     * Could mean they've never set up notifications or their settings were deleted.
     */
    data object NotConfigured : NotificationSettingsState

    /**
     * User has notification settings configured.
     * Check [settings.enabled] to determine if notifications are active.
     */
    data class Configured(val settings: NotificationSettings) : NotificationSettingsState
}
