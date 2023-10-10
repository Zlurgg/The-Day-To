package com.jbrightman.thedayto.feature_thedayto.presentation.entry.display_entries

import com.jbrightman.thedayto.feature_thedayto.domain.model.entry.TheDayToEntry
import com.jbrightman.thedayto.feature_thedayto.domain.util.entry.EntryOrder

sealed class EntriesEvent {
    data class Order(val entryOrder: EntryOrder): EntriesEvent()
    data class DeleteEntry(val entry: TheDayToEntry): EntriesEvent()
    data object RestoreEntry: EntriesEvent()
    data object ToggleOrderSection: EntriesEvent()
//    data class ChangeYear(val year: Int) : EntriesEvent()
}