package uk.co.zlurgg.thedayto.di

import androidx.lifecycle.SavedStateHandle
import org.koin.android.ext.koin.androidContext
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.dsl.module
import uk.co.zlurgg.thedayto.feature_daily_entry.presentation.add_edit_daily_entry.AddEditEntryViewModel
import uk.co.zlurgg.thedayto.feature_daily_entry.presentation.display_daily_entries.EntriesViewModel
import uk.co.zlurgg.thedayto.feature_sign_in.presentation.SignInViewModel

val addEditEntryModule = module {
    viewModel { (savedStateHandle: SavedStateHandle) ->
        AddEditEntryViewModel(
            preferencesRepository = get(),
            dailyEntryUseCases = get(),
            moodColorUseCases = get(),
            savedStateHandle = savedStateHandle
        )
    }
}

val entriesModule = module {
    viewModel {
        EntriesViewModel(
            entryUseCase = get()
        )
    }
}

val signInModule = module {
    viewModel {
        SignInViewModel()
    }
}