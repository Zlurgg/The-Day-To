package com.example.thedayto.ui

import androidx.lifecycle.ViewModelProvider.AndroidViewModelFactory
import androidx.lifecycle.createSavedStateHandle
import androidx.lifecycle.viewmodel.CreationExtras
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.example.thedayto.TheDayToApplication
import com.example.thedayto.ui.screens.EntryViewModel

object TheDayToViewModelProvider {
    val Factory = viewModelFactory {

        initializer {
            EntryViewModel(
                this.createSavedStateHandle(),
                entryApp().container.entryRepository
            )
        }
    }
}

fun CreationExtras.entryApp(): TheDayToApplication =
    (this[AndroidViewModelFactory.APPLICATION_KEY] as TheDayToApplication)