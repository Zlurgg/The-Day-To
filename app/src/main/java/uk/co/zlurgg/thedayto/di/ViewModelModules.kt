package uk.co.zlurgg.thedayto.di

import androidx.lifecycle.SavedStateHandle
import org.koin.dsl.module
import org.koin.core.module.dsl.viewModel
import uk.co.zlurgg.thedayto.journal.ui.editor.EditorViewModel
import uk.co.zlurgg.thedayto.journal.ui.overview.OverviewViewModel
import uk.co.zlurgg.thedayto.auth.ui.SignInViewModel

val editorModule = module {
    viewModel { (savedStateHandle: SavedStateHandle) ->
        EditorViewModel(
            entryUseCases = get(),
            moodColorUseCases = get(),
            savedStateHandle = savedStateHandle
        )
    }
}

val overviewModule = module {
    viewModel {
        OverviewViewModel(
            entryUseCase = get(),
            googleAuthUiClient = get(),
            authStateRepository = get(),
            notificationRepository = get()
        )
    }
}

val signInModule = module {
    viewModel {
        SignInViewModel(
            googleAuthUiClient = get(),
            authStateRepository = get(),
            entryUseCases = get()
        )
    }
}