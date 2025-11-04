package uk.co.zlurgg.thedayto.journal.ui.overview.state

sealed interface OverviewUiEvent {
    data class ShowSnackbar(val message: String) : OverviewUiEvent
    data object NavigateToSignIn : OverviewUiEvent
    data class NavigateToEditor(val entryId: Int?) : OverviewUiEvent
    data object RequestNotificationPermission : OverviewUiEvent
    data object ShowSystemNotificationWarning : OverviewUiEvent
    data object ShowPermissionPermanentlyDeniedDialog : OverviewUiEvent
    data object ShowSignOutDialog : OverviewUiEvent
    data object ShowTutorialDialog : OverviewUiEvent
}
