package com.jbrightman.thedayto.feature_thedayto.domain.use_case.entry

import com.jbrightman.thedayto.feature_thedayto.domain.model.entry.TheDayToEntry
import com.jbrightman.thedayto.feature_thedayto.domain.repository.entry.TheDayToRepository

class UpdateEntry(
    private val repository: TheDayToRepository
) {
    suspend operator fun invoke(entry: TheDayToEntry) {
        return repository.updateEntry(entry)
    }
}