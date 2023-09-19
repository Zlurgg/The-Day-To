package com.jbrightman.thedayto.feature_thedayto.domain.use_case.entry

data class EntryUseCases(
    val getEntries: GetEntriesUseCase,
    val deleteEntry: DeleteEntryUseCase,
    val addEntry: AddEntry,
    val getEntry: GetEntry
)