package uk.co.zlurgg.thedayto.journal.domain.usecases.editor

import uk.co.zlurgg.thedayto.journal.domain.usecases.shared.entry.GetEntryByDateUseCase
import uk.co.zlurgg.thedayto.journal.domain.usecases.shared.entry.GetEntryUseCase
import uk.co.zlurgg.thedayto.journal.domain.usecases.shared.moodcolor.GetMoodColorUseCase
import uk.co.zlurgg.thedayto.journal.domain.usecases.shared.moodcolor.GetMoodColorsUseCase
import uk.co.zlurgg.thedayto.journal.domain.usecases.shared.moodcolor.SaveMoodColorUseCase
import uk.co.zlurgg.thedayto.journal.domain.usecases.shared.moodcolor.SetMoodColorFavoriteUseCase

data class EditorUseCases(
    // Entry UseCases
    val getEntryUseCase: GetEntryUseCase,
    val getEntryByDateUseCase: GetEntryByDateUseCase,
    val addEntryUseCase: AddEntryUseCase,
    // MoodColor UseCases
    val getMoodColorUseCase: GetMoodColorUseCase,
    val getMoodColors: GetMoodColorsUseCase,
    val saveMoodColor: SaveMoodColorUseCase,
    val setMoodColorFavorite: SetMoodColorFavoriteUseCase,
    // Tutorial UseCases
    val checkEditorTutorialSeen: CheckEditorTutorialSeenUseCase,
    val markEditorTutorialSeen: MarkEditorTutorialSeenUseCase,
)
