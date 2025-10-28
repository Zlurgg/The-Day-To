package uk.co.zlurgg.thedayto.journal.domain.usecases.overview

data class OverviewUseCases(
    val getEntries: GetEntriesUseCase,
    val deleteEntry: DeleteEntryUseCase,
    val restoreEntry: RestoreEntryUseCase,
    val getEntryByDate: GetEntryByDateUseCase,
    val updateEntryUseCase: UpdateEntryUseCase,
    val setupNotification: SetupNotificationUseCase,
    val checkNotificationPermission: CheckNotificationPermissionUseCase
)