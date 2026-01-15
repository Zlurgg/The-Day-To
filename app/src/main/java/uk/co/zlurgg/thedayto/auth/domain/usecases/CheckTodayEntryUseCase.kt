package uk.co.zlurgg.thedayto.auth.domain.usecases

import io.github.zlurgg.core.domain.result.getOrNull
import uk.co.zlurgg.thedayto.journal.domain.repository.EntryRepository
import java.time.LocalDate
import java.time.ZoneOffset

/**
 * UseCase to check if an entry exists for today's date.
 * Used by SignInViewModel to determine post-login navigation.
 *
 * Returns the timestamp if entry exists, null otherwise.
 */
class CheckTodayEntryUseCase(
    private val entryRepository: EntryRepository
) {
    suspend operator fun invoke(): Long? {
        val todayStart = LocalDate.now().atStartOfDay().toEpochSecond(ZoneOffset.UTC)
        val entry = entryRepository.getEntryByDate(todayStart).getOrNull()
        return if (entry != null) todayStart else null
    }
}
