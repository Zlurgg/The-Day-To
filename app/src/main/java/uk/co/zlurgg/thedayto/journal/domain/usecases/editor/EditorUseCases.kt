package uk.co.zlurgg.thedayto.journal.domain.usecases.editor

import uk.co.zlurgg.thedayto.journal.domain.usecases.shared.entry.GetEntryByDateUseCase
import uk.co.zlurgg.thedayto.journal.domain.usecases.shared.entry.GetEntryUseCase
import uk.co.zlurgg.thedayto.journal.domain.usecases.shared.moodcolor.AddMoodColorUseCase
import uk.co.zlurgg.thedayto.journal.domain.usecases.shared.moodcolor.DeleteMoodColorUseCase
import uk.co.zlurgg.thedayto.journal.domain.usecases.shared.moodcolor.GetMoodColorUseCase
import uk.co.zlurgg.thedayto.journal.domain.usecases.shared.moodcolor.GetMoodColorsUseCase
import uk.co.zlurgg.thedayto.journal.domain.usecases.shared.moodcolor.UpdateMoodColorUseCase

data class EditorUseCases(
    // Entry UseCases
    val getEntryUseCase: GetEntryUseCase,
    val getEntryByDateUseCase: GetEntryByDateUseCase,
    val addEntryUseCase: AddEntryUseCase,
    // MoodColor UseCases
    val getMoodColorUseCase: GetMoodColorUseCase,
    val addMoodColorUseCase: AddMoodColorUseCase,
    val deleteMoodColor: DeleteMoodColorUseCase,
    val getMoodColors: GetMoodColorsUseCase,
    val updateMoodColorUseCase: UpdateMoodColorUseCase,
    // Tutorial UseCases
    val checkEditorTutorialSeen: CheckEditorTutorialSeenUseCase,
    val markEditorTutorialSeen: MarkEditorTutorialSeenUseCase
)