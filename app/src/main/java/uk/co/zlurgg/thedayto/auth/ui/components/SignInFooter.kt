package uk.co.zlurgg.thedayto.auth.ui.components

import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.text.style.TextAlign
import uk.co.zlurgg.thedayto.core.ui.theme.paddingMedium

@Composable
fun SignInFooter(
    showButton: Boolean,
    modifier: Modifier = Modifier
) {
    Spacer(modifier = Modifier.height(paddingMedium))
    Text(
        text = "Continue with Google",
        style = MaterialTheme.typography.bodySmall,
        color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.4f),
        textAlign = TextAlign.Center,
        modifier = modifier.alpha(if (showButton) 1f else 0f)
    )
}
