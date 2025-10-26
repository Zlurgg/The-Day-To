package uk.co.zlurgg.thedayto.di

import androidx.lifecycle.SavedStateHandle
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.dsl.module
import uk.co.zlurgg.thedayto.journal.ui.editor.EditorViewModel
import uk.co.zlurgg.thedayto.journal.ui.overview.OverviewViewModel
import uk.co.zlurgg.thedayto.auth.ui.SignInViewModel

val editorModule = module {
    viewModel { (savedStateHandle: SavedStateHandle) ->
        EditorViewModel(
            preferencesRepository = get(),
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
            preferencesRepository = get()
        )
    }
}

val signInModule = module {
    viewModel {
        SignInViewModel(
            googleAuthUiClient = get(),
            preferencesRepository = get()
        )
    }
}