package uk.co.zlurgg.thedayto.core.ui

sealed class Screen(
    val route: String
) {
    data object OverviewScreen : Screen("overview_screen")
    data object EditorScreen : Screen("editor_screen")
    data object SignInScreen : Screen("sign_in_screen")
    data object MoodColorScreen : Screen("mood_color_screen")
    data object NotificationTestScreen : Screen("notification_test_screen")
}