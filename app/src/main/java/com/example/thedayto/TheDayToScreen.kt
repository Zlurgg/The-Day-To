package com.example.thedayto

import androidx.annotation.StringRes
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController

enum class TheDayToScreen(@StringRes val title: Int) {
    Home(title = R.string.home),
    Mood(title = R.string.mood),
}

@Composable
fun TheDayToApp() {
    val navController = rememberNavController()
    NavHost(
        navController = navController,
        startDestination = TheDayToScreen.Home.name,
        modifier = Modifier.padding(24.dp)

    ) {
        composable(TheDayToScreen.Home.name) {
            HomeScreen(
                onNextButtonClicked = {
                    navController.navigate(TheDayToScreen.Mood.name)
                },
            )
        }
        composable(TheDayToScreen.Mood.name) {
            MoodScreen()
        }
    }
}

@Composable
fun HomeScreen(
    onNextButtonClicked: () -> Unit,
    modifier: Modifier = Modifier
) {
    Button(
        onClick = onNextButtonClicked
    ) {
        Text("Mood")
    }
}

@Preview(showBackground = true)
@Composable
fun HomeScreenPreview() {
    HomeScreen(
        onNextButtonClicked = {},
        modifier = Modifier.fillMaxSize().padding(24.dp)
    )
}

@Composable
fun MoodScreen(
) {
    Text("Mood")
}


@Preview(showBackground = true)
@Composable
fun MoodScreenPreview() {
    MoodScreen()
}


/*@Composable
fun CalenderScreen() {
    Text("Calender")
}

@Preview(showBackground = true)
@Composable
fun CalenderScreenPreview() {
    CalenderScreen()
}*/

