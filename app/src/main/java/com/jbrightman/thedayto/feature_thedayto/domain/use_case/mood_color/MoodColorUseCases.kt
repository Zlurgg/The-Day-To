package com.jbrightman.thedayto.feature_thedayto.domain.use_case.mood_color

data class MoodColorUseCases(
    val getMoodColor: GetMoodColor,
    val addMoodColor: AddMoodColor,
    val deleteMoodColor: DeleteMoodColorUseCase,
    val getMoodColors: GetMoodColorsUseCase,
    val updateMoodColor: UpdateMoodColor
)