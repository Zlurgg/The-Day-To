package uk.co.zlurgg.thedayto.journal.domain.usecases.moodcolormanagement

import uk.co.zlurgg.thedayto.journal.domain.usecases.shared.moodcolor.DeleteMoodColorUseCase
import uk.co.zlurgg.thedayto.journal.domain.usecases.shared.moodcolor.GetSortedMoodColorsUseCase
import uk.co.zlurgg.thedayto.journal.domain.usecases.shared.moodcolor.RestoreMoodColorUseCase
import uk.co.zlurgg.thedayto.journal.domain.usecases.shared.moodcolor.SaveMoodColorUseCase
import uk.co.zlurgg.thedayto.journal.domain.usecases.shared.moodcolor.SetMoodColorFavoriteUseCase

/**
 * Aggregator for Mood Color Management screen use cases.
 * Groups all use cases needed by MoodColorManagementViewModel.
 */
data class MoodColorManagementUseCases(
    val getSortedMoodColors: GetSortedMoodColorsUseCase,
    val saveMoodColor: SaveMoodColorUseCase,
    val deleteMoodColor: DeleteMoodColorUseCase,
    val restoreMoodColor: RestoreMoodColorUseCase,
    val setFavorite: SetMoodColorFavoriteUseCase,
)
