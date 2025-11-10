package uk.co.zlurgg.thedayto.di

import androidx.lifecycle.SavedStateHandle
import org.koin.dsl.module
import org.koin.core.module.dsl.viewModel
import uk.co.zlurgg.thedayto.journal.ui.editor.EditorViewModel
import uk.co.zlurgg.thedayto.journal.ui.overview.OverviewViewModel
import uk.co.zlurgg.thedayto.journal.ui.stats.StatsViewModel
import uk.co.zlurgg.thedayto.auth.ui.SignInViewModel

val editorModule = module {
    viewModel { (savedStateHandle: SavedStateHandle) ->
        EditorViewModel(
            editorUseCases = get(),
            savedStateHandle = savedStateHandle
        )
    }
}

val overviewModule = module {
    viewModel {
        OverviewViewModel(
            overviewUseCases = get()
        )
    }
}

val signInModule = module {
    viewModel {
        SignInViewModel(
            signInUseCases = get()
        )
    }
}

val statsModule = module {
    viewModel {
        StatsViewModel(
            getEntriesUseCase = get(),
            statsUseCases = get()
        )
    }
}