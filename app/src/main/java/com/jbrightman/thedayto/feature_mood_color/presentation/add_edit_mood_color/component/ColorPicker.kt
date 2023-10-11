package com.jbrightman.thedayto.feature_mood_color.presentation.add_edit_mood_color.component

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.github.skydoves.colorpicker.compose.ColorEnvelope
import com.github.skydoves.colorpicker.compose.HsvColorPicker
import com.github.skydoves.colorpicker.compose.rememberColorPickerController
import com.jbrightman.thedayto.feature_mood_color.presentation.add_edit_mood_color.AddEditMoodColorEvent
import com.jbrightman.thedayto.feature_mood_color.presentation.add_edit_mood_color.AddEditMoodColorViewModel

@Composable
fun ColorPicker(
        viewModel: AddEditMoodColorViewModel = hiltViewModel()
) {
    val controller = rememberColorPickerController()
    HsvColorPicker(
        modifier = Modifier
            .fillMaxWidth()
            .height(450.dp)
            .padding(10.dp),
        controller = controller,
        onColorChanged = { colorEnvelope: ColorEnvelope ->
            viewModel.onEvent(AddEditMoodColorEvent.EnteredColor(colorEnvelope.color))
        }
    )
}