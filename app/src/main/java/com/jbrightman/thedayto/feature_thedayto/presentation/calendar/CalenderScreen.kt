package com.jbrightman.thedayto.feature_thedayto.presentation.calendar

import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.jbrightman.thedayto.feature_thedayto.presentation.util.Screen
import java.time.LocalDate

@Composable
fun CalenderScreen(
    navController: NavController,
    modifier: Modifier
) {

    /** Get dimensions for calender (on refresh update rows) **/
    val date = LocalDate.now()
    val columns = 7
    val rows = 5

    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(16.dp),
        verticalAlignment = Alignment.Top
    ) {
        Text("current date: $date")
    }


    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Button(
            onClick = {
                navController.navigate(route = Screen.EntriesScreen.route)
            }
        ) {
            Text("Add Entry")
        }
    }
}
