package uk.co.zlurgg.thedayto.auth.ui.state

sealed interface SignInUiEvent {
    data class ShowSnackbar(val message: String) : SignInUiEvent
}
