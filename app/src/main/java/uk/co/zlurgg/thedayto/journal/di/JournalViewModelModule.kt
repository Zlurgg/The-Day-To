package uk.co.zlurgg.thedayto.journal.di

import androidx.lifecycle.SavedStateHandle
import org.koin.core.module.dsl.viewModel
import org.koin.dsl.module
import uk.co.zlurgg.thedayto.journal.ui.editor.EditorViewModel
import uk.co.zlurgg.thedayto.journal.ui.moodcolormanagement.MoodColorManagementViewModel
import uk.co.zlurgg.thedayto.journal.ui.overview.OverviewViewModel
import uk.co.zlurgg.thedayto.journal.ui.stats.StatsViewModel

val journalViewModelModule = module {

    // Editor ViewModel - requires SavedStateHandle for navigation arguments
    viewModel { (savedStateHandle: SavedStateHandle) ->
        EditorViewModel(
            editorUseCases = get(),
            savedStateHandle = savedStateHandle
        )
    }

    // Overview ViewModel
    viewModel {
        OverviewViewModel(
            overviewUseCases = get()
        )
    }

    // Stats ViewModel
    viewModel {
        StatsViewModel(
            getEntriesUseCase = get(),
            statsUseCases = get()
        )
    }

    // MoodColorManagement ViewModel
    viewModel {
        MoodColorManagementViewModel(
            moodColorManagementUseCases = get()
        )
    }
}
