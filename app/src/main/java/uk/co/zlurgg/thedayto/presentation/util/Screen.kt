package uk.co.zlurgg.thedayto.presentation.util

sealed class Screen(
    val route: String
) {
    data object EntriesScreen: Screen("entries_screen")
    data object AddEditEntryScreen: Screen("add_edit_entry_screen")
    data object SignInScreen: Screen("sign_in_screen")
    data object AddEditMoodColorScreen: Screen("add_edit_mood_color_screen")
    data object NotificationTestScreen: Screen("notification_test_screen")
}