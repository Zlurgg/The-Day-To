package uk.co.zlurgg.thedayto.journal.domain.usecases.entry

data class EntryUseCases(
    val getEntries: GetEntriesUseCase,
    val deleteEntry: DeleteEntryUseCase,
    val addEntryUseCase: AddEntryUseCase,
    val getEntryUseCase: GetEntryUseCase,
    val updateEntryUseCase: UpdateEntryUseCase
)