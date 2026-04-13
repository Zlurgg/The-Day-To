package uk.co.zlurgg.thedayto.journal.domain.model

/**
 * Domain-level errors for entry operations in the Editor.
 * Formatted for the user by [EntryErrorFormatter] in the UI layer.
 */
sealed interface EntryError : uk.co.zlurgg.thedayto.core.domain.error.Error {
    data object NotFound : EntryError
    data object LoadFailed : EntryError
    data object DateLoadFailed : EntryError
    data object NoMoodSelected : EntryError
    data object SaveFailed : EntryError
    data object RetryFailed : EntryError
}
