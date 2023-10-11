package com.jbrightman.thedayto

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import com.jbrightman.thedayto.presentation.TheDayToApp
import com.jbrightman.thedayto.ui.theme.TheDayToTheme
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
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
