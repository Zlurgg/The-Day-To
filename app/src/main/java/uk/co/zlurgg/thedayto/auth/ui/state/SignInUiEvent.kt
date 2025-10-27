package uk.co.zlurgg.thedayto.auth.ui.state

sealed interface SignInUiEvent {
    data object NavigateToOverview : SignInUiEvent
    data class NavigateToEditor(val entryDate: Long) : SignInUiEvent
    data class ShowSnackbar(val message: String) : SignInUiEvent
}
