package com.jbrightman.thedayto.feature_thedayto.domain.use_case.entry

import com.jbrightman.thedayto.feature_thedayto.domain.model.entry.TheDayToEntry
import com.jbrightman.thedayto.feature_thedayto.domain.repository.entry.TheDayToRepository

class DeleteEntryUseCase (
    private val repository: TheDayToRepository
) {
    suspend operator fun invoke(entry: TheDayToEntry) {
        repository.deleteEntry(entry)
    }
}
