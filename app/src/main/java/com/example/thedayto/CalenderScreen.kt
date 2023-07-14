package com.example.thedayto

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview

@Composable
fun CalenderScreen(
    mood: String,
    onReturnButtonClicked: () -> Unit
) {
    val id = if (mood == "sad_face") {
        R.drawable.sad_face
    } else {
        R.drawable.happy_face
    }
    Column() {
        Image(
            painter = painterResource(id = id),
            contentDescription = "displayed mood"
        )
        Button(
            modifier = Modifier.fillMaxWidth(),
            onClick = { onReturnButtonClicked() }
        ) {
            Text("Home")
        }
    }
}

@Preview(showBackground = true)
@Composable
fun CalenderScreenPreview() {
    CalenderScreen(
        mood = "",
        onReturnButtonClicked = {}
    )
}