package com.example.thedayto

import androidx.annotation.StringRes
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController

/** screen names **/
enum class TheDayToScreen(@StringRes val title: Int) {
    Home(title = R.string.home),
    Calender(title = R.string.calender),
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TheDayToApp() {
    val navController = rememberNavController()
    var mood = ""
    NavHost(
        navController = navController,
        startDestination = TheDayToScreen.Home.name,
        modifier = Modifier.padding(24.dp)
    ) {
        composable(TheDayToScreen.Home.name) {
            HomeScreen(
                onSubmitMoodButtonClicked = {
                    navController.navigate(TheDayToScreen.Calender.name)
                    mood = it
                }
            )
        }
        composable(TheDayToScreen.Calender.name) {
            CalenderScreen(
                mood = mood,
                onReturnButtonClicked = {
                backToHome(navController)
            })
        }
    }
}

@Composable
fun HomeScreen(
    onSubmitMoodButtonClicked: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    Column() {
        Text(
            modifier = Modifier.fillMaxWidth(),
            text = "How're you feeling today?")
        Row(
            horizontalArrangement = Arrangement.SpaceBetween,
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp)
        ) {
            Button(
                onClick = { onSubmitMoodButtonClicked("happy_face") },
            ) {
                Image(
                    painter = painterResource(R.drawable.happy_face),
                    contentDescription = "happy mood"
                )
            }
            Button(
                onClick = { onSubmitMoodButtonClicked("sad_face") },
            ) {
                Image(
                    painter = painterResource(R.drawable.sad_face),
                    contentDescription = "sad mood"
                )
            }
        }
    }

}

@Preview(showBackground = true)
@Composable
fun HomeScreenPreview() {
    HomeScreen(
        onSubmitMoodButtonClicked = {},
        modifier = Modifier
            .fillMaxSize())
}

private fun backToHome(
    navController: NavHostController
) {
    navController.popBackStack(TheDayToScreen.Home.name, inclusive = false)
}
