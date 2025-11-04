package uk.co.zlurgg.thedayto.core.domain.usecases.notifications

/**
 * Use case interface to check if an entry exists for today.
 *
 * This interface is defined in core to avoid coupling NotificationWorker
 * to the journal feature module. The implementation is provided by the
 * journal module and injected via Koin.
 *
 * Following Clean Architecture and Dependency Inversion Principle:
 * - Core defines the contract (interface)
 * - Journal implements the contract
 * - Core (NotificationWorker) depends on abstraction, not concrete implementation
 */
interface CheckTodayEntryExistsUseCase {
    /**
     * Check if an entry exists for today's date.
     *
     * @return true if an entry exists for today, false otherwise
     */
    suspend operator fun invoke(): Boolean
}