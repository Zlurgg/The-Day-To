package com.example.thedayto.ui.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.thedayto.R

@Composable
fun MoodScreen(
    onSubmitMoodButtonClicked: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = Modifier
        .padding(8.dp)
    ) {
        Text(
            modifier = Modifier.fillMaxWidth().padding(16.dp),
            text = "How're you feeling today?",
            fontWeight = FontWeight.Bold,
            fontSize =  25.sp
        )
        Spacer(modifier = Modifier.height(10.dp))
        Row(
            horizontalArrangement = Arrangement.SpaceBetween,
            modifier = Modifier
                .fillMaxWidth()
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
