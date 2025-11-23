package uk.co.zlurgg.thedayto.journal.ui.overview.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import uk.co.zlurgg.thedayto.R
import uk.co.zlurgg.thedayto.core.ui.theme.paddingMedium
import uk.co.zlurgg.thedayto.core.ui.theme.paddingSmall
import uk.co.zlurgg.thedayto.journal.ui.overview.util.UiConstants

@Composable
fun EmptyState(
    modifier: Modifier = Modifier,
    onCreateEntry: () -> Unit = {},
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(paddingMedium),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = "📝",
            style = MaterialTheme.typography.displayLarge
        )
        Spacer(modifier = Modifier.height(paddingSmall))
        Text(
            text = stringResource(R.string.empty_state_title),
            style = MaterialTheme.typography.headlineMedium,
            color = MaterialTheme.colorScheme.onSurface
        )
        Spacer(modifier = Modifier.height(UiConstants.EMPTY_STATE_SPACING))
        Text(
            text = stringResource(R.string.empty_state_description),
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(horizontal = UiConstants.EMPTY_STATE_PADDING_HORIZONTAL)
        )
        Spacer(modifier = Modifier.height(paddingMedium))
        Button(
            onClick = onCreateEntry
        ) {
            Text(stringResource(R.string.empty_state_create_button))
        }
    }
}
