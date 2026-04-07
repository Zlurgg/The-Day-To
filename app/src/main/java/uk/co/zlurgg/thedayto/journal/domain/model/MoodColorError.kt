package uk.co.zlurgg.thedayto.journal.domain.model

/**
 * Domain-level errors for mood color operations.
 * Does NOT expose data layer types (Clean Architecture).
 */
sealed interface MoodColorError : uk.co.zlurgg.thedayto.core.domain.error.Error {
    data object BlankName : MoodColorError
    data object NameTooLong : MoodColorError
    data object InvalidColor : MoodColorError
    data object DuplicateName : MoodColorError
    data object LimitReached : MoodColorError
    data object NotFound : MoodColorError
    data object DatabaseError : MoodColorError
}
