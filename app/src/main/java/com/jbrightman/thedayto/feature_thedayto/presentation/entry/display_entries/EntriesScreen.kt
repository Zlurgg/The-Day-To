package com.jbrightman.thedayto.feature_thedayto.presentation.entry.display_entries

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.List
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.SnackbarResult
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.jbrightman.thedayto.feature_thedayto.presentation.entry.display_entries.components.CalenderDay
import com.jbrightman.thedayto.feature_thedayto.presentation.entry.display_entries.components.EntryItem
import com.jbrightman.thedayto.feature_thedayto.presentation.entry.display_entries.components.OrderSection
import com.jbrightman.thedayto.feature_thedayto.presentation.util.Screen
import com.jbrightman.thedayto.feature_thedayto.presentation.util.datestampToFormattedDay
import com.jbrightman.thedayto.feature_thedayto.presentation.util.datestampToMonthValue
import com.jbrightman.thedayto.feature_thedayto.presentation.util.datestampToYearValue
import com.jbrightman.thedayto.ui.theme.paddingMedium
import com.jbrightman.thedayto.ui.theme.paddingSmall
import com.jbrightman.thedayto.ui.theme.paddingVeryLarge
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.ZoneOffset

@Composable
fun EntriesScreen(
    navController: NavController,
    viewModel: EntriesViewModel = hiltViewModel(),
) {
    val state = viewModel.state.value
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()
    var isAddButtonVisible by remember { mutableStateOf(true) }

    val currentDate = LocalDate.now()
    val daysInMonth = currentDate.lengthOfMonth()
    val dates = MutableList(daysInMonth) { it }

    Scaffold(
        topBar = {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(paddingSmall),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Your entries",
                    style = MaterialTheme.typography.headlineMedium
                )
                IconButton(
                    onClick = {
                        viewModel.onEvent(EntriesEvent.ToggleOrderSection)
                    },
                ) {
                    Icon(
                        imageVector = Icons.Default.List,
                        contentDescription = "Sort"
                    )
                }
            }
            AnimatedVisibility(
                visible = state.isOrderSectionVisible,
                enter = fadeIn() + slideInVertically(),
                exit = fadeOut() + slideOutVertically()
            ) {
                OrderSection(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = paddingVeryLarge),
                    entryOrder = state.entryOrder,
                    onOrderChange = {
                        viewModel.onEvent(EntriesEvent.Order(it))
                    }
                )
            }
        },
        floatingActionButton = {
            /** if statement for controlling fab visibility set below when entries are created **/
            if (isAddButtonVisible) {
                FloatingActionButton(
                    onClick = {
                        navController.navigate(Screen.AddEditEntryScreen.route)
                    },
                    modifier = Modifier.background(MaterialTheme.colorScheme.primary)
                ) {
                    Icon(imageVector = Icons.Default.Add, contentDescription = "Add Entry")
                }
            }
        },
        snackbarHost = { SnackbarHost(snackbarHostState) },
        content = { padding ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .padding(paddingMedium)
            ) {
                LazyVerticalGrid(
                    modifier = Modifier
                        .systemBarsPadding(),

                    columns = GridCells.Fixed(7),
                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {

                    items(dates) {
                        state.entries.forEach { entry ->
                            if ((it+1).toString() == datestampToFormattedDay(entry.dateStamp)
                                && currentDate.monthValue.toString() == datestampToMonthValue(entry.dateStamp)
                                && currentDate.year.toString() == datestampToYearValue(entry.dateStamp)
                            ) {
                                CalenderDay(
                                    entry = entry,
                                    modifier = Modifier
                                        .clickable {
                                            navController.navigate(
                                                Screen.AddEditEntryScreen.route +
                                                        "?entryId=${entry.id}&entryColor=${entry.color}"
                                            )
                                        }
                                )
                            } else {
                                Box(contentAlignment = Alignment.Center) {
                                    Text(
                                        text = "${it + 1}",
                                        style = MaterialTheme.typography.headlineSmall,
                                        color = MaterialTheme.colorScheme.onSurface,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                }
                            }
                            if (entry.dateStamp == currentDate.atStartOfDay().toEpochSecond(
                                    ZoneOffset.UTC)) {
                                isAddButtonVisible = false
                            }

                        }
                    }
                }
                Spacer(modifier = Modifier.height(paddingMedium))
                LazyColumn(modifier = Modifier.fillMaxSize()) {
                    items(state.entries) { entry ->
                        if (currentDate.monthValue.toString() == datestampToMonthValue(entry.dateStamp)
                            && currentDate.year.toString() == datestampToYearValue(entry.dateStamp)) {
                            EntryItem(
                                entry = entry,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable {
                                        navController.navigate(
                                            Screen.AddEditEntryScreen.route +
                                                    "?entryId=${entry.id}&entryColor=${entry.color}"
                                        )
                                    },
                                onDeleteClick = {
                                    viewModel.onEvent(EntriesEvent.DeleteEntry(entry))
                                    scope.launch {
                                        val result = snackbarHostState.showSnackbar(
                                            message = "Entry deleted",
                                            actionLabel = "Undo"
                                        )
                                        if (result == SnackbarResult.ActionPerformed) {
                                            viewModel.onEvent(EntriesEvent.RestoreEntry)
                                        }
                                    }
                                }
                            )
                        }
                        Spacer(modifier = Modifier.height(paddingMedium))
                    }
                }
            }
        }
    )
}