package com.jbrightman.thedayto.feature_thedayto.presentation.entry.add_edit_entry

import androidx.compose.animation.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Color.Companion.DarkGray
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.DialogProperties
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.jbrightman.thedayto.R
import com.jbrightman.thedayto.feature_thedayto.domain.model.TheDayToEntry
import com.jbrightman.thedayto.feature_thedayto.presentation.entry.add_edit_entry.components.TransparentHintTextField
import com.jbrightman.thedayto.feature_thedayto.presentation.util.longToFormattedDateText
import com.vanpra.composematerialdialogs.MaterialDialog
import com.vanpra.composematerialdialogs.datetime.date.DatePickerDefaults
import com.vanpra.composematerialdialogs.datetime.date.datepicker
import com.vanpra.composematerialdialogs.rememberMaterialDialogState
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.ZoneOffset

@Composable
fun AddEditEntryScreen(
    navController: NavController,
    entryColor: Int,
    viewModel: AddEditEntryViewModel = hiltViewModel()
) {
    val titleState = viewModel.entryTitle.value
    val moodState = viewModel.entryMood.value
    val dateState = viewModel.entryDate.value
    val contentState = viewModel.entryContent.value

    val dateDialogState = rememberMaterialDialogState()

    val noteBackgroundAnimatatable = remember {
        Animatable(
            Color(if (entryColor != -1) entryColor else viewModel.entryColor.value)
        )
    }
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    LaunchedEffect(key1 = true) {
        viewModel.eventFlow.collectLatest { event ->
            when (event) {
                is AddEditEntryViewModel.UiEvent.ShowSnackbar -> {
                    snackbarHostState.showSnackbar(
                        message = event.message
                    )
                }

                is AddEditEntryViewModel.UiEvent.SaveEntry -> {
                    navController.navigateUp()
                }
            }
        }
    }

    Scaffold(
        floatingActionButton = {
            FloatingActionButton(
                onClick = {
                    viewModel.onEvent(AddEditEntryEvent.SaveEntry)
                },
                modifier = Modifier.background(MaterialTheme.colorScheme.primary)
            ) {
                Icon(imageVector = Icons.Default.Check, contentDescription = "Save entry")
            }
        },
        snackbarHost = { SnackbarHost(snackbarHostState) },
        content = { padding ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .background(noteBackgroundAnimatatable.value)
                    .padding(16.dp)
            ) {
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
                                        noteBackgroundAnimatatable.animateTo(
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
                Spacer(modifier = Modifier.height(16.dp))

                // Date Picker
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .fillMaxWidth()
                ) {
                    Text(
                        style = MaterialTheme.typography.headlineSmall,
                        color = DarkGray,
                        text = longToFormattedDateText(dateState.date),
                    )
                    Icon(
                        painter = painterResource(id = R.drawable.ic_drop_down_arrow),
                        tint = DarkGray,
                        contentDescription = "Date picker dropdown button",
                        modifier = Modifier
                            .clickable(
                                onClick = { dateDialogState.show() },
                                role = Role.Button,
                            )
                    )
                    MaterialDialog(
                        dialogState = dateDialogState,
                        properties = DialogProperties(
                            dismissOnBackPress = true
                        ),
                        backgroundColor = MaterialTheme.colorScheme.background,
                        elevation = 10.dp,
                        onCloseRequest = {},
                        buttons = {
                            positiveButton(text = "Ok")
                            negativeButton(text = "Cancel")
                        }
                    ) {
                        datepicker(
                            initialDate = LocalDate.now(),
                            title = "Pick a date",
                            colors = DatePickerDefaults.colors(),
                            allowedDateValidator = {
                                !it.isAfter(LocalDate.now())
                            }
                        ) {
                            viewModel.onEvent(
                                AddEditEntryEvent.EnteredDate(
                                    it.atStartOfDay().toEpochSecond(
                                        ZoneOffset.UTC
                                    )
                                )
                            )
                        }
                    }
                    if (dateState.date == 0L) {
                        viewModel.onEvent(
                            AddEditEntryEvent.EnteredDate(
                                LocalDate.now().atStartOfDay().toEpochSecond(
                                    ZoneOffset.UTC
                                )
                            )
                        )
                    }
                }
                Spacer(modifier = Modifier.height(16.dp))
                // Title
                TransparentHintTextField(
                    text = titleState.text,
                    hint = titleState.hint,
                    onValueChange = {
                        viewModel.onEvent(AddEditEntryEvent.EnteredTitle(it))
                    },
                    onFocusChange = {
                        viewModel.onEvent(AddEditEntryEvent.ChangeTitleFocus(it))
                    },
                    isHintVisible = titleState.isHintVisible,
                    singleLine = true,
                    textStyle = MaterialTheme.typography.headlineSmall
                )
                Spacer(modifier = Modifier.height(16.dp))
                // Mood
                TransparentHintTextField(
                    text = moodState.mood,
                    hint = moodState.hint,
                    onValueChange = {
                        viewModel.onEvent(AddEditEntryEvent.EnteredMood(it))
                    },
                    onFocusChange = {
                        viewModel.onEvent(AddEditEntryEvent.ChangeMoodFocus(it))
                    },
                    isHintVisible = moodState.isHintVisible,
                    singleLine = true,
                    textStyle = MaterialTheme.typography.headlineSmall
                )
                Spacer(modifier = Modifier.height(16.dp))

                // Content
                TransparentHintTextField(
                    text = contentState.text,
                    hint = contentState.hint,
                    onValueChange = {
                        viewModel.onEvent(AddEditEntryEvent.EnteredContent(it))
                    },
                    onFocusChange = {
                        viewModel.onEvent(AddEditEntryEvent.ChangeContentFocus(it))
                    },
                    isHintVisible = contentState.isHintVisible,
                    textStyle = MaterialTheme.typography.bodyLarge,
                    modifier = Modifier.fillMaxHeight()
                )
            }
        }
    )
}