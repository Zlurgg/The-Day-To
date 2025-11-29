package uk.co.zlurgg.thedayto.journal.domain.usecases.moodcolormanagement

import uk.co.zlurgg.thedayto.journal.domain.usecases.shared.entry.GetMoodColorEntryCountsUseCase
import uk.co.zlurgg.thedayto.journal.domain.usecases.shared.moodcolor.AddMoodColorUseCase
import uk.co.zlurgg.thedayto.journal.domain.usecases.shared.moodcolor.DeleteMoodColorUseCase
import uk.co.zlurgg.thedayto.journal.domain.usecases.shared.moodcolor.GetMoodColorsUseCase
import uk.co.zlurgg.thedayto.journal.domain.usecases.shared.moodcolor.UpdateMoodColorNameUseCase
import uk.co.zlurgg.thedayto.journal.domain.usecases.shared.moodcolor.UpdateMoodColorUseCase

/**
 * Aggregator for Mood Color Management screen use cases.
 * Groups all use cases needed by MoodColorManagementViewModel.
 */
data class MoodColorManagementUseCases(
    val getMoodColors: GetMoodColorsUseCase,
    val addMoodColor: AddMoodColorUseCase,
    val updateMoodColor: UpdateMoodColorUseCase,
    val updateMoodColorName: UpdateMoodColorNameUseCase,
    val deleteMoodColor: DeleteMoodColorUseCase,
    val getMoodColorEntryCounts: GetMoodColorEntryCountsUseCase
)
