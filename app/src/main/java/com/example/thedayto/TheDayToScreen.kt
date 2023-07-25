package com.example.thedayto

import androidx.annotation.StringRes
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.thedayto.ui.theme.white
import kotlin.random.Random

/** screen names **/
enum class TheDayToScreen(@StringRes val title: Int) {
    Home(title = R.string.home),
    Calender(title = R.string.calender),
}

@Composable
fun TheDayToApp() {
    val navController = rememberNavController()
    val calendarInputList by remember { mutableStateOf(createCalendarList()) }
    var clickedCalendarElem by remember { mutableStateOf<CalendarInput?>(null) }

    /** instance of a mood class to set mood on a specific day
     * and pass it to the calender (best to store it in data base and retrieve it
     * **/
    val status = Status()
    status.id = Random.nextInt(10).toLong()
    status.date = DateUtil().getCurrentDate()

    println("check current date: " + DateUtil().getCurrentDate())

    /** get current month to create calender **/
    val month = DateUtil().getCurrentMonthInMMMMFormat()

    NavHost(
        navController = navController,
        startDestination = TheDayToScreen.Home.name,
        modifier = Modifier.padding(24.dp)
    ) {
        composable(TheDayToScreen.Home.name) {
            HomeScreen(
                onSubmitMoodButtonClicked = {
                    navController.navigate(TheDayToScreen.Calender.name)
                    status.mood = it
                }
            )
        }
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
                    month = month,
                    modifier = Modifier
                        .padding(10.dp)
                        .fillMaxWidth()
                        .aspectRatio(1.3f),
                    status = status,
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
                        Text(
                            if (it.contains("Day")) it else "- $it",
                            color = white,
                            fontWeight = FontWeight.SemiBold,
                            fontSize = if (it.contains("Day")) 25.sp else 18.sp
                        )

                        /** get the value of the mood from status on that day
                         * using the date
                         *
                         * check the date + month + year and then match with the status for that date
                         * this needs moving to the dayToScreen, calender screen is really just the calender
                         */
                        val day = it.substringAfterLast(" ")
                        val day2 = day.substringBefore(":")

                        val clickedDate = "2023" + "-" +
                                DateUtil().changeMonthFromMMMMToMMFormat(month) + "-" +
                                day2
                        println("date: ${status.date} versus $clickedDate")

                        if (clickedDate == status.date) {
                            val id = if (status.mood == "sad_face") {
                                R.drawable.sad_face
                            } else {
                                R.drawable.happy_face
                            }
                            Image(
                                painter = painterResource(id = id),
                                contentDescription = "display mood"
                            )
                        }
                    }
                }
            }
        }
    }
}

private fun backToHome(
    navController: NavHostController
) {
    navController.popBackStack(TheDayToScreen.Home.name, inclusive = false)
}