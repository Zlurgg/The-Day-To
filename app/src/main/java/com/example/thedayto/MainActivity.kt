package com.example.thedayto

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import com.example.thedayto.ui.TheDayToApp
import com.example.thedayto.ui.theme.TheDayToTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            TheDayToTheme {
                TheDayToApp()
            }
        }
    }
}
