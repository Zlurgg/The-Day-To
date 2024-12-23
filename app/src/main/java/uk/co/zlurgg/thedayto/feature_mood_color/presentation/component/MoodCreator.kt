package uk.co.zlurgg.thedayto.feature_mood_color.presentation.component

import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import org.koin.androidx.compose.koinViewModel
import uk.co.zlurgg.thedayto.feature_mood_color.presentation.AddEditMoodColorEvent
import uk.co.zlurgg.thedayto.feature_mood_color.presentation.AddEditMoodColorViewModel

@Composable
fun MoodCreator(
    viewModel: AddEditMoodColorViewModel = koinViewModel()
) {
    val moodState = viewModel.moodColorMood.value

    MoodTextField(
        mood = moodState.mood,
        hint = moodState.hint,
        onValueChange = {
            viewModel.onEvent(AddEditMoodColorEvent.EnteredMood(it))
        },
        onFocusChange = {
            viewModel.onEvent(AddEditMoodColorEvent.ChangeMoodFocus(it))
        },
        isHintVisible = moodState.isHintVisible,
        singleLine = true,
        textStyle = MaterialTheme.typography.headlineSmall
    )
}