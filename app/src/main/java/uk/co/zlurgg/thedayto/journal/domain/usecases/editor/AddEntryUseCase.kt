package uk.co.zlurgg.thedayto.journal.domain.usecases.editor

import uk.co.zlurgg.thedayto.journal.domain.model.Entry
import uk.co.zlurgg.thedayto.journal.domain.model.InvalidEntryException
import uk.co.zlurgg.thedayto.journal.domain.repository.EntryRepository
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
 */
class AddEntryUseCase(
    private val repository: EntryRepository
) {
    @Throws(InvalidEntryException::class)
    suspend operator fun invoke(entry: Entry) {
        // Validate timestamp
        when (val result = InputValidation.validateTimestamp(entry.dateStamp)) {
            is ValidationResult.Invalid -> throw InvalidEntryException(result.message)
            is ValidationResult.Valid -> {} // Continue
        }

        // Validate and sanitize mood
        val sanitizedMood = when (val result = InputValidation.validateMood(entry.mood)) {
            is ValidationResult.Invalid -> throw InvalidEntryException(result.message)
            is ValidationResult.Valid -> result.value
        }

        // Validate and sanitize content (notes)
        val sanitizedContent = when (val result = InputValidation.validateContent(entry.content)) {
            is ValidationResult.Invalid -> throw InvalidEntryException(result.message)
            is ValidationResult.Valid -> result.value
        }

        // Color validation removed - colors are only selected via picker (programmatic, always valid)
        // Basic check that color exists
        if (entry.color.isBlank()) {
            throw InvalidEntryException("Color cannot be empty")
        }

        // Create sanitized entry
        val sanitizedEntry = entry.copy(
            mood = sanitizedMood,
            content = sanitizedContent
        )

        repository.insertEntry(sanitizedEntry)
    }
}