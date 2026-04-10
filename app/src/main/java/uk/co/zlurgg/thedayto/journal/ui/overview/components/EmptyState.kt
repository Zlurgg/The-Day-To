package uk.co.zlurgg.thedayto.journal.ui.overview.components

import android.content.res.Configuration
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import uk.co.zlurgg.thedayto.R
import uk.co.zlurgg.thedayto.core.ui.theme.TheDayToTheme
import uk.co.zlurgg.thedayto.core.ui.theme.paddingMedium

@Composable
fun EmptyState(
    isCurrentMonth: Boolean,
    modifier: Modifier = Modifier,
    onCreateEntry: () -> Unit = {},
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(paddingMedium),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        if (isCurrentMonth) {
            Button(
                onClick = onCreateEntry,
                shape = RoundedCornerShape(12.dp),
            ) {
                Text(
                    text = stringResource(R.string.empty_state_create_button),
                    style = MaterialTheme.typography.headlineMedium,
                )
            }
        } else {
            Text(
                text = stringResource(R.string.empty_state_title),
                style = MaterialTheme.typography.headlineMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}

@Preview(name = "Current Month - Light", showBackground = true)
@Preview(name = "Current Month - Dark", uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun EmptyStateCurrentMonthPreview() {
    TheDayToTheme {
        EmptyState(
            isCurrentMonth = true,
            onCreateEntry = {},
        )
    }
}

@Preview(name = "Past Month - Light", showBackground = true)
@Preview(name = "Past Month - Dark", uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun EmptyStatePastMonthPreview() {
    TheDayToTheme {
        EmptyState(
            isCurrentMonth = false,
            onCreateEntry = {},
        )
    }
}
