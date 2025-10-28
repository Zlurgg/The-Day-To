package uk.co.zlurgg.thedayto.journal.domain.usecases.editor

data class EditorUseCases(
    // Entry UseCases
    val getEntryUseCase: GetEntryUseCase,
    val addEntryUseCase: AddEntryUseCase,
    // MoodColor UseCases
    val getMoodColorUseCase: GetMoodColorUseCase,
    val addMoodColorUseCase: AddMoodColorUseCase,
    val deleteMoodColor: DeleteMoodColorUseCase,
    val getMoodColors: GetMoodColorsUseCase,
    val updateMoodColorUseCase: UpdateMoodColorUseCase
)