package com.jbrightman.thedayto

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import com.google.android.gms.auth.api.identity.Identity
import com.jbrightman.thedayto.feature_login.presentation.GoogleAuthUiClient
import com.jbrightman.thedayto.presentation.TheDayToApp
import com.jbrightman.thedayto.ui.theme.TheDayToTheme
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    private val googleAuthUiClient by lazy {
        GoogleAuthUiClient(
            context = applicationContext,
            oneTapClient = Identity.getSignInClient(applicationContext)
        )
    }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            TheDayToTheme {
                TheDayToApp(
                    googleAuthUiClient
                )
            }
        }
    }
}
