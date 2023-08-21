package com.example.thedayto

import androidx.compose.runtime.Composable
import com.example.thedayto.ui.screens.NavGraphs
import com.ramcosta.composedestinations.DestinationsNavHost

@Composable
fun TheDayToApp() {
    DestinationsNavHost(navGraph = NavGraphs.root)
}

