package com.example.thedayto.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
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
import com.example.thedayto.R

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