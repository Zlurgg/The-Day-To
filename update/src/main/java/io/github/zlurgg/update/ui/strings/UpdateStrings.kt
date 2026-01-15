package io.github.zlurgg.update.ui.strings

/**
 * String resources for UpdateDialog.
 * Pass custom strings for localization, or use defaults for English.
 */
data class UpdateDialogStrings(
    val title: String = "Update Available",
    val versionLabel: String = "Version",
    val whatsNewLabel: String = "What's New",
    val downloadButton: String = "Download",
    val notNowButton: String = "Not Now"
)

/**
 * String resources for UpToDateDialog.
 * Pass custom strings for localization, or use defaults for English.
 */
data class UpToDateDialogStrings(
    val title: String = "You're Up to Date",
    val currentVersionLabel: String = "Current Version",
    val inThisVersionLabel: String = "In This Version",
    val okButton: String = "OK"
)
