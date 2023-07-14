package com.example.thedayto

import androidx.annotation.StringRes
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController

/** screen names **/
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
                onSubmitMoodButtonClicked = {
                    navController.navigate(TheDayToScreen.Mood.name)

                },
            )
        }
        composable(TheDayToScreen.Mood.name) {
            MoodScreen("")
        }
    }
}

class MoodViewModel {

}

@Composable
fun HomeScreen(
    onSubmitMoodButtonClicked: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        horizontalArrangement = Arrangement.SpaceBetween,
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp)
    ) {
        Button(
            onClick = { onSubmitMoodButtonClicked("Happy") },
        ) {
            Image(
                painter = painterResource(R.drawable.happy_face),
                contentDescription = "Happy face"
            )
        }
        Button(
            onClick = { onSubmitMoodButtonClicked("Sad") },
        ) {
            Image(
                painter = painterResource(R.drawable.sad_face),
                contentDescription = "Sad face"
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
fun HomeScreenPreview() {
    HomeScreen(
        onSubmitMoodButtonClicked = {},
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp)
    )
}

@Composable
fun MoodScreen(mood: String) {
    Text("Mood = $mood")
}


@Preview(showBackground = true)
@Composable
fun MoodScreenPreview() {
    MoodScreen("")
}

