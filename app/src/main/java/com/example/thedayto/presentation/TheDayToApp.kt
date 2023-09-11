package com.example.thedayto.presentation

import androidx.compose.runtime.Composable
import com.example.thedayto.presentation.entry.NavGraphs
import com.ramcosta.composedestinations.DestinationsNavHost

@Composable
fun TheDayToApp() {
    DestinationsNavHost(navGraph = NavGraphs.root)
}