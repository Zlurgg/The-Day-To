package com.jbrightman.thedayto.feature_thedayto.presentation.entry.add_edit_entry.components

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.AnimationVector4D
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.jbrightman.thedayto.feature_thedayto.domain.model.TheDayToEntry
import com.jbrightman.thedayto.feature_thedayto.presentation.entry.add_edit_entry.AddEditEntryEvent
import com.jbrightman.thedayto.feature_thedayto.presentation.entry.add_edit_entry.AddEditEntryViewModel
import kotlinx.coroutines.launch

@Composable
fun ColorSelector(
    entryBackgroundAnimatatable: Animatable<Color, AnimationVector4D>,
    viewModel: AddEditEntryViewModel = hiltViewModel()
) {
    val scope = rememberCoroutineScope()

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        TheDayToEntry.entryColors.forEach { color ->
            val colorInt = color.toArgb()
            Box(
                modifier = Modifier
                    .size(50.dp)
                    .shadow(15.dp, CircleShape)
                    .clip(CircleShape)
                    .background(color)
                    .border(
                        width = 3.dp,
                        color = if (viewModel.entryColor.value == colorInt) {
                            Color.Black
                        } else {
                            Color.Transparent
                        },
                        shape = CircleShape
                    )
                    .clickable {
                        scope.launch {
                            entryBackgroundAnimatatable.animateTo(
                                targetValue = Color(colorInt),
                                animationSpec = tween(
                                    durationMillis = 500
                                )
                            )
                        }
                        viewModel.onEvent(AddEditEntryEvent.ChangeColor(colorInt))
                    }
            )
        }
    }
}