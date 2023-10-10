package com.jbrightman.thedayto.feature_thedayto.presentation.add_edit_mood_color.component

import androidx.compose.animation.Animatable
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Surface
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.github.skydoves.colorpicker.compose.ColorEnvelope
import com.github.skydoves.colorpicker.compose.HsvColorPicker
import com.github.skydoves.colorpicker.compose.rememberColorPickerController

@androidx.compose.runtime.Composable

fun ColorPicker(
    //    viewModel: AddEditMoodColorViewModel = hiltViewModel()
) {
    val controller = rememberColorPickerController()
    HsvColorPicker(
        modifier = Modifier
            .fillMaxWidth()
            .height(450.dp)
            .padding(10.dp),
        controller = controller,
        onColorChanged = { colorEnvelope: ColorEnvelope ->
            // update vm with changed color value
//            viewModel.onEvent(AddEditMoodColorViewModel.OnColorChanged(colorEnvelope.color))
        }
    )
}