package com.example.thedayto

import androidx.annotation.StringRes
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.thedayto.data.calender.CalendarInput
import com.example.thedayto.ui.TheDayToViewModelProvider
import com.example.thedayto.ui.screens.CalenderScreen
import com.example.thedayto.ui.screens.EntryViewModel
import com.example.thedayto.ui.screens.MoodScreen
import com.example.thedayto.ui.screens.NoteScreen
import com.example.thedayto.util.CalenderUtil
import com.example.thedayto.util.DateUtil
import kotlinx.coroutines.launch

/** screen names replace with navhost and topbar moving below into a navhost class to navigate the app **/
enum class TheDayToScreen(@StringRes val title: Int) {
    Mood(title = R.string.mood),
    Note(title = R.string.note),
    Calender(title = R.string.calender),
}

@Composable
fun TheDayToApp(
   viewModel: EntryViewModel = viewModel(factory = TheDayToViewModelProvider.Factory)
) {
    val navController = rememberNavController()
    val calendarInputList by remember { mutableStateOf(CalenderUtil().createCalendarList()) }
    var clickedCalendarElem by remember { mutableStateOf<CalendarInput?>(null) }

    /** instance of a mood class to set mood on a specific day
     * and pass it to the calender (best to store it in data base and retrieve it
     * **/
//    entry.date = DateUtil().getCurrentDate()
    
    /** get current month to create calender **/
    val month = DateUtil().getCurrentMonthInMMMMFormat()

    val coroutineScope = rememberCoroutineScope()

    NavHost(
        navController = navController,
        startDestination = TheDayToScreen.Mood.name,
        modifier = Modifier.padding(24.dp)
    ) {
        composable(TheDayToScreen.Mood.name) {
            MoodScreen(
                onSubmitMoodButtonClicked = {
//                    entry.mood = it
                    navController.navigate(TheDayToScreen.Note.name)
                },
                entryUiState = viewModel.entriesUiState,
                onEntryValueChange = viewModel::updateUiState,
                onSaveClick = {
                    coroutineScope.launch {
                        viewModel.saveEntry()
                        navController.navigate(TheDayToScreen.Note.name)
                    }
                }
            )
        }
        composable(TheDayToScreen.Note.name) {
            NoteScreen(
                onSubmitNoteButtonClicked = {
//                    entry.note = it
                    navController.navigate(TheDayToScreen.Calender.name)
                },
                entryUiState = viewModel.entriesUiState,
                onEntryValueChange = viewModel::updateUiState,
                onSaveClick = {
                    coroutineScope.launch {
                        viewModel.saveEntry()
                        navController.navigate(TheDayToScreen.Calender.name)
                    }
                }
            )
        }
        /** move calender screen into the calender screen class (that at the moment just holds the calender layout
         * possible two screens what is currently calender screen as calender layout util and the below as the screen calling it**/
        composable(TheDayToScreen.Calender.name) {
            Box(
                modifier = Modifier
                    .fillMaxSize(),
                contentAlignment = Alignment.TopCenter
            ) {
                CalenderScreen(
                    calendarInput = calendarInputList,
                    onDayClick = { day ->
                        clickedCalendarElem = calendarInputList.first { it.day == day }
                    },
                    modifier = Modifier
                        .padding(10.dp)
                        .fillMaxWidth()
                        .aspectRatio(1.3f),
//                    entry = entry,
//                    entryList = entryUi,
                    onReturnButtonClicked = {
                        backToHome(navController)
                    })
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(10.dp)
                        .align(Alignment.Center)
                ) {
                    clickedCalendarElem?.toDos?.forEach {
//                        Text(
//                            if (it.contains("Day")) it else "- $it",
//                            color = white,
//                            fontWeight = FontWeight.SemiBold,
//                            fontSize = if (it.contains("Day")) 25.sp else 18.sp
//                        )

                        /** get the value of the mood from status on that day
                         * using the date
                         *
                         * check the date + month + year and then match with the status for that date
                         * this needs moving to the dayToScreen, calender screen is really just the calender
                         */

                        /** date should be fetched along with status and id
                         * formatting it here to remove extras
                         * move this into date util to be one method that gets the current month in MMMMM from date
                         * */
                        val day = it.substringAfterLast(" ")
                        val day2 = day.substringBefore(":")

                        val clickedDate = "2023" + "-" +
                                DateUtil().changeMonthFromMMMMToMMFormat(month) + "-" +
                                day2

                        /**
                         * display the mood from status if the status date matches the selected one
                         * rather than displaying the mood here display the note from entry
                         **/
//                        if (clickedDate == entry.date) {
//                            val id = if (entry.mood == "sad_face") {
//                                R.drawable.sad_face
//                            } else {
//                                R.drawable.happy_face
//                            }
//                            Image(
//                                painter = painterResource(id = id),
//                                contentDescription = "display mood"
//                            )
                            /**
                             * if user had anything else to add that day
                             **/
                        println("entry id:" + viewModel.entriesUiState.entryDetails.toString())
//                            if (entry.note != "") {
//                                Text(text = "Extra thoughts from ${entry.date}!",
//                                    fontWeight = FontWeight.Bold,
//                                    fontSize =  25.sp,
//                                    modifier = Modifier.padding(16.dp)
//                                    )
//                                Text(text = entry.note,
//                                    fontSize =  25.sp,
//                                    modifier = Modifier.padding(16.dp))
//                            }
//                        }
                    }
                }
            }
        }
    }
}

private fun backToHome(
    navController: NavHostController
) {
    navController.popBackStack(TheDayToScreen.Mood.name, inclusive = false)
}