package uk.co.zlurgg.thedayto.journal.ui.overview.components

import android.content.res.Configuration
import androidx.compose.foundation.layout.Box
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import uk.co.zlurgg.thedayto.R
import uk.co.zlurgg.thedayto.core.ui.theme.TheDayToTheme

@Composable
fun SettingsMenu(
    onOpenNotificationSettings: () -> Unit,
    onShowTutorial: () -> Unit,
    onNavigateToStats: () -> Unit,
    onSignOut: () -> Unit
) {
    var expanded by remember { mutableStateOf(false) }

    Box {
        IconButton(onClick = { expanded = true }) {
            Icon(
                imageVector = Icons.Default.MoreVert,
                contentDescription = stringResource(R.string.settings)
            )
        }

        DropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false },
            modifier = Modifier.align(Alignment.TopEnd)
        ) {
            // Statistics
            DropdownMenuItem(
                text = { Text("Statistics") },
                onClick = {
                    expanded = false
                    onNavigateToStats()
                },
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Default.BarChart,
                        contentDescription = null
                    )
                }
            )

            // Always show "Notification Settings" - permission handled inside dialog
            DropdownMenuItem(
                text = { Text(stringResource(R.string.notification_settings)) },
                onClick = {
                    expanded = false
                    onOpenNotificationSettings()
                }
            )

            DropdownMenuItem(
                text = { Text(stringResource(R.string.help_and_tutorial)) },
                onClick = {
                    expanded = false
                    onShowTutorial()
                }
            )

            DropdownMenuItem(
                text = { Text(stringResource(R.string.sign_out)) },
                onClick = {
                    expanded = false
                    onSignOut()
                }
            )
        }
    }
}

@Preview(name = "Light Mode", showBackground = true)
@Preview(name = "Dark Mode", uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun SettingsMenuPreview() {
    TheDayToTheme {
        SettingsMenu(
            onOpenNotificationSettings = {},
            onShowTutorial = {},
            onNavigateToStats = {},
            onSignOut = {}
        )
    }
}
