package uk.co.zlurgg.thedayto.journal.ui.overview.state

sealed interface OverviewUiEvent {
    data class ShowSnackbar(val message: String, val actionLabel: String? = null) : OverviewUiEvent
    data object RequestNotificationPermission : OverviewUiEvent
    data object ShowSystemNotificationWarning : OverviewUiEvent
    data object ShowPermissionPermanentlyDeniedDialog : OverviewUiEvent
    data object ShowSignOutDialog : OverviewUiEvent
    data object ShowHelpDialog : OverviewUiEvent
    data object ShowAboutDialog : OverviewUiEvent
}
