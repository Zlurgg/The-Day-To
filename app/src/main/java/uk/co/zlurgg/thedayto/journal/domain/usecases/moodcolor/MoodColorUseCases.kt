package uk.co.zlurgg.thedayto.journal.domain.usecases.moodcolor

data class MoodColorUseCases(
    val getMoodColorUseCase: GetMoodColorUseCase,
    val addMoodColorUseCase: AddMoodColorUseCase,
    val deleteMoodColor: DeleteMoodColorUseCase,
    val getMoodColors: GetMoodColorsUseCase,
    val updateMoodColorUseCase: UpdateMoodColorUseCase
)