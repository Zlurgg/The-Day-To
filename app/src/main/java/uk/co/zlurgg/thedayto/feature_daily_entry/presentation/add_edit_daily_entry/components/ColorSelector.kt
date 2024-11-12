package uk.co.zlurgg.thedayto.feature_daily_entry.presentation.add_edit_daily_entry.components
//
//import androidx.compose.animation.core.Animatable
//import androidx.compose.animation.core.AnimationVector4D
//import androidx.compose.animation.core.tween
//import androidx.compose.foundation.background
//import androidx.compose.foundation.border
//import androidx.compose.foundation.clickable
//import androidx.compose.foundation.layout.Arrangement
//import androidx.compose.foundation.layout.Box
//import androidx.compose.foundation.layout.Row
//import androidx.compose.foundation.layout.fillMaxWidth
//import androidx.compose.foundation.layout.padding
//import androidx.compose.foundation.layout.size
//import androidx.compose.foundation.shape.CircleShape
//import androidx.compose.runtime.Composable
//import androidx.compose.runtime.rememberCoroutineScope
//import androidx.compose.ui.Modifier
//import androidx.compose.ui.draw.clip
//import androidx.compose.ui.draw.shadow
//import androidx.compose.ui.graphics.Color
//import androidx.compose.ui.graphics.toArgb
//import androidx.compose.ui.unit.dp
//import uk.co.zlurgg.thedayto.feature_thedayto.domain.model.mood_color.MoodColor
//import kotlinx.coroutines.launch
//
//@Composable
//fun ColorSelector(
//    entryBackgroundAnimatatable: Animatable<Color, AnimationVector4D>,
////    viewModel: AddEditMoodColorViewModel = koinViewModel()
//) {
//    val scope = rememberCoroutineScope()
//
//    Row(
//        modifier = Modifier
//            .fillMaxWidth()
//            .padding(8.dp),
//        horizontalArrangement = Arrangement.SpaceBetween
//    ) {
//        MoodColor.defaultColors.forEach { color ->
//            val colorInt = color.toArgb()
//            Box(
//                modifier = Modifier
//                    .size(50.dp)
//                    .shadow(15.dp, CircleShape)
//                    .clip(CircleShape)
//                    .background(color)
//                    .border(
//                        width = 3.dp,
//                        color = Color(colorInt),
//                        shape = CircleShape
//                    )
//                    .clickable {
//                        scope.launch {
//                            entryBackgroundAnimatatable.animateTo(
//                                targetValue = Color(colorInt),
//                                animationSpec = tween(
//                                    durationMillis = 500
//                                )
//                            )
//                        }
////                        AddEditMoodColorViewModel.onEvent(AddEditEntryEvent.ChangeColor(colorInt))
//                    }
//            )
//        }
//    }
//}