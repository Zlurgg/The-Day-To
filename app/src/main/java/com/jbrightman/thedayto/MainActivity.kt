package com.jbrightman.thedayto

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.viewmodel.compose.viewModel
import com.google.android.gms.auth.api.identity.Identity
import com.jbrightman.thedayto.domain.repository.PrefRepository
import com.jbrightman.thedayto.feature_daily_entry.domain.model.DailyEntry
import com.jbrightman.thedayto.feature_daily_entry.presentation.display_daily_entries.EntriesViewModel
import com.jbrightman.thedayto.feature_sign_in.presentation.GoogleAuthUiClient
import com.jbrightman.thedayto.presentation.TheDayToApp
import com.jbrightman.thedayto.ui.theme.TheDayToTheme
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

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
