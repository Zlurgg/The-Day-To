package uk.co.zlurgg.thedayto.feature_mood_color.domain.use_case

data class MoodColorUseCases(
    val getMoodColor: GetMoodColor,
    val addMoodColor: AddMoodColor,
    val deleteMoodColor: DeleteMoodColorUseCase,
    val getMoodColors: GetMoodColorsUseCase,
    val updateMoodColor: UpdateMoodColor
)