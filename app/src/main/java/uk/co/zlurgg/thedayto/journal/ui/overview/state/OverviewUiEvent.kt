package uk.co.zlurgg.thedayto.journal.ui.overview.state

sealed interface OverviewUiEvent {
    data class ShowSnackbar(val message: String) : OverviewUiEvent
    data object NavigateToSignIn : OverviewUiEvent
    data object RequestNotificationPermission : OverviewUiEvent
}
