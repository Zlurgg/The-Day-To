package uk.co.zlurgg.thedayto.core.ui.notifications

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TimePicker
import androidx.compose.material3.TimePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import uk.co.zlurgg.thedayto.R
import uk.co.zlurgg.thedayto.core.ui.theme.paddingMedium
import uk.co.zlurgg.thedayto.core.ui.theme.paddingMediumSmall
import uk.co.zlurgg.thedayto.core.ui.theme.paddingSmall

/**
 * Notification settings dialog with enable/disable toggle and time picker.
 *
 * Uses Material 3 TimePicker (analog clock) for selecting notification time.
 * When user toggles ON without permission, triggers permission request.
 *
 * @param enabled Initial enabled state
 * @param hour Initial hour (0-23)
 * @param minute Initial minute (0-59)
 * @param hasPermission Whether notification permission is granted
 * @param onDismiss Callback when dialog is dismissed
 * @param onRequestPermission Callback to request notification permission
 * @param onSave Callback when settings are saved (enabled, hour, minute)
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NotificationSettingsDialog(
    enabled: Boolean,
    hour: Int,
    minute: Int,
    hasPermission: Boolean,
    onDismiss: () -> Unit,
    onRequestPermission: () -> Unit,
    onSave: (enabled: Boolean, hour: Int, minute: Int) -> Unit
) {
    var isEnabled by remember { mutableStateOf(enabled) }
    // Track a key to force TimePickerState recreation when preset chips are clicked
    var timePickerKey by remember { mutableStateOf(0) }
    var selectedHour by remember { mutableStateOf(hour) }
    var selectedMinute by remember { mutableStateOf(minute) }
    val timePickerState = remember(timePickerKey) {
        TimePickerState(
            initialHour = selectedHour,
            initialMinute = selectedMinute,
            is24Hour = true
        )
    }

    // Auto-enable when permission is granted
    LaunchedEffect(hasPermission, enabled) {
        if (hasPermission && enabled && !isEnabled) {
            isEnabled = true
        }
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(text = stringResource(R.string.notification_settings_title))
        },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth()
            ) {
                // Enable/Disable Toggle
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = stringResource(R.string.enable_daily_reminders),
                        style = MaterialTheme.typography.bodyLarge,
                        modifier = Modifier.weight(1f)
                    )
                    Switch(
                        checked = isEnabled,
                        onCheckedChange = { newValue ->
                            if (newValue && !hasPermission) {
                                // User wants to enable but doesn't have permission
                                onRequestPermission()
                            } else {
                                // Either disabling or already has permission
                                isEnabled = newValue
                            }
                        }
                    )
                }

                Spacer(modifier = Modifier.height(paddingMedium))

                // Time Picker (only show when enabled)
                if (isEnabled) {
                    Text(
                        text = stringResource(R.string.notification_time),
                        style = MaterialTheme.typography.titleMedium,
                        modifier = Modifier.padding(bottom = paddingSmall)
                    )

                    // Quick time presets
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceEvenly
                    ) {
                        FilterChip(
                            selected = timePickerState.hour == 8 && timePickerState.minute == 0,
                            onClick = {
                                selectedHour = 8
                                selectedMinute = 0
                                timePickerKey++
                            },
                            label = {
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    Text(
                                        text = stringResource(R.string.preset_morning),
                                        style = MaterialTheme.typography.labelSmall
                                    )
                                    Text(
                                        text = "8:00",
                                        style = MaterialTheme.typography.bodySmall
                                    )
                                }
                            }
                        )
                        FilterChip(
                            selected = timePickerState.hour == 12 && timePickerState.minute == 0,
                            onClick = {
                                selectedHour = 12
                                selectedMinute = 0
                                timePickerKey++
                            },
                            label = {
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    Text(
                                        text = stringResource(R.string.preset_noon),
                                        style = MaterialTheme.typography.labelSmall
                                    )
                                    Text(
                                        text = "12:00",
                                        style = MaterialTheme.typography.bodySmall
                                    )
                                }
                            }
                        )
                        FilterChip(
                            selected = timePickerState.hour == 20 && timePickerState.minute == 0,
                            onClick = {
                                selectedHour = 20
                                selectedMinute = 0
                                timePickerKey++
                            },
                            label = {
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    Text(
                                        text = stringResource(R.string.preset_evening),
                                        style = MaterialTheme.typography.labelSmall
                                    )
                                    Text(
                                        text = "20:00",
                                        style = MaterialTheme.typography.bodySmall
                                    )
                                }
                            }
                        )
                    }

                    Spacer(modifier = Modifier.height(paddingMediumSmall))

                    TimePicker(
                        state = timePickerState,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    onSave(isEnabled, timePickerState.hour, timePickerState.minute)
                }
            ) {
                Text(text = stringResource(R.string.save))
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(text = stringResource(R.string.cancel))
            }
        }
    )
}
