package uk.co.zlurgg.thedayto.feature_mood_color.presentation.component

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.github.skydoves.colorpicker.compose.ColorEnvelope
import com.github.skydoves.colorpicker.compose.HsvColorPicker
import com.github.skydoves.colorpicker.compose.rememberColorPickerController
import org.koin.androidx.compose.koinViewModel
import uk.co.zlurgg.thedayto.feature_mood_color.presentation.AddEditMoodColorEvent
import uk.co.zlurgg.thedayto.feature_mood_color.presentation.AddEditMoodColorViewModel

@Composable
fun ColorPicker(
    viewModel: AddEditMoodColorViewModel = koinViewModel()
) {
    val controller = rememberColorPickerController()
    HsvColorPicker(
        modifier = Modifier
            .fillMaxWidth()
            .height(250.dp)
            .padding(10.dp),
        controller = controller,
        onColorChanged = { colorEnvelope: ColorEnvelope ->
            viewModel.onEvent(AddEditMoodColorEvent.EnteredColor(colorEnvelope))
        }
    )
}