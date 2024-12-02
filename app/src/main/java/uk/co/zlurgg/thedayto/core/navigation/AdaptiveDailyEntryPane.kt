package uk.co.zlurgg.thedayto.core.navigation

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import org.koin.compose.koinInject
import uk.co.zlurgg.thedayto.feature_sign_in.presentation.GoogleAuthUiClient

@Composable
fun AdaptiveDailyEntryPane(
    modifier: Modifier = Modifier,
    googleAuthUiClient: GoogleAuthUiClient = koinInject()

) {
    
}