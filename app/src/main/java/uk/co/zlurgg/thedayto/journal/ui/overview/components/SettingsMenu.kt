package uk.co.zlurgg.thedayto.journal.ui.overview.components

import androidx.compose.foundation.layout.Box
import androidx.compose.material.icons.Icons
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
import uk.co.zlurgg.thedayto.R

@Composable
fun SettingsMenu(
    hasNotificationPermission: Boolean,
    onRequestNotificationPermission: () -> Unit,
    onSignOut: () -> Unit
) {
    var expanded by remember { mutableStateOf(false) }

    Box {
        IconButton(onClick = { expanded = true }) {
            Icon(
                imageVector = Icons.Default.MoreVert,
                contentDescription = "Settings"
            )
        }

        DropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false },
            modifier = Modifier.align(Alignment.TopEnd)
        ) {
            // Only show "Enable Notifications" if permission not granted
            if (!hasNotificationPermission) {
                DropdownMenuItem(
                    text = { Text(stringResource(R.string.enable_notifications)) },
                    onClick = {
                        expanded = false
                        onRequestNotificationPermission()
                    }
                )
            }

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
