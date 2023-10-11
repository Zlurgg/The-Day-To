package com.jbrightman.thedayto.feature_login.presentation

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.jbrightman.thedayto.presentation.util.Screen
import com.jbrightman.thedayto.ui.theme.paddingMedium

@Composable
fun LoginScreen(
    navController: NavController
) {
    Column(
        verticalArrangement = Arrangement.Center,
        modifier = Modifier
            .fillMaxSize()
            .padding(paddingMedium)
    ) {
        Column {
            Text(text ="This is a login screen", color = Color.White)
            Spacer(modifier = Modifier.padding(vertical = 8.dp))
            Row {
                Button(onClick = { navController.navigate(Screen.AddEditEntryScreen.route) }) {
                    Text(text ="Add", color = Color.White)
                }
                Spacer(modifier = Modifier.padding(horizontal = 8.dp))
                Button(onClick = { navController.navigate(Screen.EntriesScreen.route) }) {
                    Text(text ="View", color = Color.White)
                }
            }
        }
    }
}