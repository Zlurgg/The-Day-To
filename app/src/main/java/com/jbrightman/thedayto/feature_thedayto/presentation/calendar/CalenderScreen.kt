package com.jbrightman.thedayto.feature_thedayto.presentation.calendar

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.jbrightman.thedayto.feature_thedayto.presentation.calendar.content.CalenderDay
import com.jbrightman.thedayto.feature_thedayto.presentation.entry.display_entries.EntriesViewModel
import com.jbrightman.thedayto.feature_thedayto.presentation.util.Screen
import com.jbrightman.thedayto.feature_thedayto.presentation.util.datestampToFormattedDay
import com.jbrightman.thedayto.feature_thedayto.presentation.util.datestampToMonthValue
import com.jbrightman.thedayto.feature_thedayto.presentation.util.datestampToYearValue
import com.jbrightman.thedayto.ui.theme.paddingMedium
import java.time.LocalDate

@Composable
fun CalenderScreen(
    navController: NavController,
    modifier: Modifier,
    viewModel: EntriesViewModel = hiltViewModel(),
) {
    val state = viewModel.state.value
    val snackbarHostState = remember { SnackbarHostState() }
    /** Get dimensions for calender (on refresh update rows) **/
    val date = LocalDate.now()
    val daysInMonth = date.lengthOfMonth()
    val dates = MutableList(daysInMonth) { it }
    Scaffold(
        topBar = {
            Row{
                Icon(
                    imageVector = Icons.Filled.ArrowBack,
                    contentDescription = "Back",
                    modifier = Modifier
                        .padding(paddingMedium)
                        .clickable {
                            navController.popBackStack()
                        }
                )
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
                            && date.monthValue.toString() == datestampToMonthValue(entry.dateStamp)
                            && date.year.toString() == datestampToYearValue(entry.dateStamp)
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
                        }
                    }
                }
                Spacer(modifier = Modifier.height(paddingMedium))
                Button(
                    onClick = {
                        navController.navigate(Screen.AddEditEntryScreen.route)
                    }) {
                    Text(text = "Add Entry")
                }
            }
        }
    )


}



