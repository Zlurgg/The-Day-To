package uk.co.zlurgg.thedayto.journal.domain.usecases.editor

import io.github.zlurgg.core.domain.result.getOrNull
import uk.co.zlurgg.thedayto.journal.domain.model.Entry
import uk.co.zlurgg.thedayto.journal.domain.model.InvalidEntryException
import uk.co.zlurgg.thedayto.journal.domain.repository.EntryRepository
import uk.co.zlurgg.thedayto.journal.domain.repository.MoodColorRepository
import uk.co.zlurgg.thedayto.journal.domain.util.InputValidation
import uk.co.zlurgg.thedayto.journal.domain.util.ValidationResult

/**
 * Use Case: Add or update a journal entry with comprehensive validation
 *
 * Security measures:
 * - Input length limits to prevent DoS/memory exhaustion
 * - Input sanitization to remove control characters
 * - Format validation for all fields
 * - User-friendly error messages
 *
 * @param repository Entry repository for persistence
 * @param moodColorRepository MoodColor repository for validation
 */
class AddEntryUseCase(
    private val repository: EntryRepository,
    private val moodColorRepository: MoodColorRepository
) {
    @Throws(InvalidEntryException::class)
    suspend operator fun invoke(entry: Entry) {
        // Validate timestamp
        when (val result = InputValidation.validateTimestamp(entry.dateStamp)) {
            is ValidationResult.Invalid -> throw InvalidEntryException(result.message)
            is ValidationResult.Valid -> {} // Continue
        }

        // Check for duplicate entry on the same date (only when creating new entry)
        if (entry.id == null) {
            val existingEntry = repository.getEntryByDate(entry.dateStamp).getOrNull()
            if (existingEntry != null) {
                throw InvalidEntryException("An entry already exists for this date")
            }
        }

        // Validate and sanitize content (notes)
        val sanitizedContent = when (val result = InputValidation.validateContent(entry.content)) {
            is ValidationResult.Invalid -> throw InvalidEntryException(result.message)
            is ValidationResult.Valid -> result.value
        }

        // Validate that moodColorId exists and is not deleted
        val moodColor = moodColorRepository.getMoodColorById(entry.moodColorId).getOrNull()
            ?: throw InvalidEntryException("Selected mood no longer exists")

        if (moodColor.isDeleted) {
            throw InvalidEntryException("Selected mood has been deleted")
        }

        // Create sanitized entry
        val sanitizedEntry = entry.copy(
            content = sanitizedContent
        )

        repository.insertEntry(sanitizedEntry)
    }
}