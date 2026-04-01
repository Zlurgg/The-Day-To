package uk.co.zlurgg.thedayto.sync.ui

import android.content.res.Configuration
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.CloudOff
import androidx.compose.material.icons.filled.CloudSync
import androidx.compose.material.icons.filled.Error
import androidx.compose.material.icons.filled.Sync
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import org.koin.androidx.compose.koinViewModel
import uk.co.zlurgg.thedayto.R
import uk.co.zlurgg.thedayto.core.ui.theme.TheDayToTheme
import uk.co.zlurgg.thedayto.core.ui.theme.paddingMedium
import uk.co.zlurgg.thedayto.core.ui.theme.paddingSmall
import uk.co.zlurgg.thedayto.sync.domain.model.SyncResult
import uk.co.zlurgg.thedayto.sync.domain.model.SyncState
import java.text.DateFormat
import java.util.Date

/**
 * Root composable - handles ViewModel, state collection, and side effects
 */
@Composable
fun SyncSettingsScreenRoot(
    viewModel: SyncSettingsViewModel = koinViewModel(),
    onNavigateBack: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(uiState.error) {
        uiState.error?.let { error ->
            snackbarHostState.showSnackbar(error)
            viewModel.onErrorDismissed()
        }
    }

    SyncSettingsScreen(
        uiState = uiState,
        snackbarHostState = snackbarHostState,
        onNavigateBack = onNavigateBack,
        onSyncToggled = viewModel::onSyncToggled,
        onSyncNowClicked = viewModel::onSyncNowClicked
    )
}

/**
 * Presenter composable - pure UI with state and callbacks
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun SyncSettingsScreen(
    uiState: SyncSettingsState,
    snackbarHostState: SnackbarHostState,
    onNavigateBack: () -> Unit,
    onSyncToggled: (Boolean) -> Unit,
    onSyncNowClicked: () -> Unit,
    modifier: Modifier = Modifier
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.sync_settings_title)) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = stringResource(R.string.sync_navigate_back)
                        )
                    }
                }
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) },
        modifier = modifier
    ) { paddingValues ->
        when {
            uiState.isLoading && !uiState.isUserSignedIn -> {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            }
            !uiState.isUserSignedIn -> {
                NotSignedInContent(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues)
                )
            }
            else -> {
                SyncSettingsContent(
                    uiState = uiState,
                    onSyncToggled = onSyncToggled,
                    onSyncNowClicked = onSyncNowClicked,
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues)
                        .verticalScroll(rememberScrollState())
                )
            }
        }
    }
}

@Composable
private fun NotSignedInContent(modifier: Modifier = Modifier) {
    Column(
        modifier = modifier.padding(paddingMedium),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            imageVector = Icons.Default.CloudOff,
            contentDescription = null,
            modifier = Modifier.size(64.dp),
            tint = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(modifier = Modifier.height(paddingMedium))
        Text(
            text = stringResource(R.string.sync_sign_in_required_title),
            style = MaterialTheme.typography.headlineSmall
        )
        Spacer(modifier = Modifier.height(paddingSmall))
        Text(
            text = stringResource(R.string.sync_sign_in_required_message),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
private fun SyncSettingsContent(
    uiState: SyncSettingsState,
    onSyncToggled: (Boolean) -> Unit,
    onSyncNowClicked: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.padding(paddingMedium),
        verticalArrangement = Arrangement.spacedBy(paddingMedium)
    ) {
        // Sync Enable Card
        SyncToggleCard(
            isSyncEnabled = uiState.isSyncEnabled,
            isLoading = uiState.isLoading,
            onSyncToggled = onSyncToggled
        )

        // Sync Status Card (only when sync is enabled)
        if (uiState.isSyncEnabled) {
            SyncStatusCard(
                syncState = uiState.syncState,
                lastSyncTimestamp = uiState.lastSyncTimestamp,
                onSyncNowClicked = onSyncNowClicked
            )
        }

        // Info Card
        SyncInfoCard()
    }
}

@Composable
private fun SyncToggleCard(
    isSyncEnabled: Boolean,
    isLoading: Boolean,
    onSyncToggled: (Boolean) -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainerLow
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(paddingMedium),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.CloudSync,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary
                )
                Spacer(modifier = Modifier.width(paddingMedium))
                Column {
                    Text(
                        text = stringResource(R.string.sync_enable_label),
                        style = MaterialTheme.typography.titleMedium
                    )
                    Text(
                        text = stringResource(R.string.sync_enable_description),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
            if (isLoading) {
                CircularProgressIndicator(
                    modifier = Modifier.size(24.dp),
                    strokeWidth = 2.dp
                )
            } else {
                Switch(
                    checked = isSyncEnabled,
                    onCheckedChange = onSyncToggled
                )
            }
        }
    }
}

@Composable
private fun SyncStatusCard(
    syncState: SyncState,
    lastSyncTimestamp: Long?,
    onSyncNowClicked: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainerLow
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(paddingMedium)
        ) {
            Text(
                text = stringResource(R.string.sync_status_title),
                style = MaterialTheme.typography.titleMedium
            )
            Spacer(modifier = Modifier.height(paddingSmall))

            // Status indicator
            SyncStateIndicator(syncState = syncState)

            // Last sync time
            lastSyncTimestamp?.let { timestamp ->
                Spacer(modifier = Modifier.height(paddingSmall))
                Text(
                    text = stringResource(
                        R.string.sync_last_sync_format,
                        formatTimestamp(timestamp)
                    ),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Spacer(modifier = Modifier.height(paddingMedium))

            // Sync Now button
            Button(
                onClick = onSyncNowClicked,
                enabled = syncState !is SyncState.Syncing,
                modifier = Modifier.fillMaxWidth()
            ) {
                Icon(
                    imageVector = Icons.Default.Sync,
                    contentDescription = null,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(paddingSmall))
                Text(stringResource(R.string.sync_now_button))
            }
        }
    }
}

@Composable
private fun SyncStateIndicator(
    syncState: SyncState,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier,
        verticalAlignment = Alignment.CenterVertically
    ) {
        when (syncState) {
            is SyncState.Idle -> {
                Icon(
                    imageVector = Icons.Default.CheckCircle,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(paddingSmall))
                Text(
                    text = stringResource(R.string.sync_status_idle),
                    style = MaterialTheme.typography.bodyMedium
                )
            }
            is SyncState.Syncing -> {
                Column(modifier = Modifier.fillMaxWidth()) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(20.dp),
                            strokeWidth = 2.dp
                        )
                        Spacer(modifier = Modifier.width(paddingSmall))
                        Text(
                            text = stringResource(R.string.sync_status_syncing),
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }
                    if (syncState.progress > 0f) {
                        Spacer(modifier = Modifier.height(paddingSmall))
                        LinearProgressIndicator(
                            progress = { syncState.progress },
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }
            }
            is SyncState.Success -> {
                Icon(
                    imageVector = Icons.Default.CheckCircle,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(paddingSmall))
                Text(
                    text = stringResource(
                        R.string.sync_status_success,
                        syncState.result.entriesUploaded + syncState.result.moodColorsUploaded,
                        syncState.result.entriesDownloaded + syncState.result.moodColorsDownloaded
                    ),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.primary
                )
            }
            is SyncState.Error -> {
                Icon(
                    imageVector = Icons.Default.Error,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.error,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(paddingSmall))
                Text(
                    text = stringResource(R.string.sync_status_error),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.error
                )
            }
        }
    }
}

@Composable
private fun SyncInfoCard(modifier: Modifier = Modifier) {
    Card(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainerLow
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(paddingMedium)
        ) {
            Text(
                text = stringResource(R.string.sync_info_title),
                style = MaterialTheme.typography.titleMedium
            )
            Spacer(modifier = Modifier.height(paddingSmall))
            Text(
                text = stringResource(R.string.sync_info_content),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

private fun formatTimestamp(timestamp: Long): String {
    val dateFormat = DateFormat.getDateTimeInstance(DateFormat.SHORT, DateFormat.SHORT)
    return dateFormat.format(Date(timestamp))
}

@Preview(name = "Light Mode", showBackground = true)
@Preview(name = "Dark Mode", uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun SyncSettingsScreenPreview() {
    TheDayToTheme {
        SyncSettingsScreen(
            uiState = SyncSettingsState(
                isUserSignedIn = true,
                isSyncEnabled = true,
                syncState = SyncState.Success(SyncResult(2, 1, 3, 0)),
                lastSyncTimestamp = System.currentTimeMillis(),
                isLoading = false
            ),
            snackbarHostState = SnackbarHostState(),
            onNavigateBack = {},
            onSyncToggled = {},
            onSyncNowClicked = {}
        )
    }
}

@Preview(name = "Sync Disabled", showBackground = true)
@Composable
private fun SyncSettingsScreenDisabledPreview() {
    TheDayToTheme {
        SyncSettingsScreen(
            uiState = SyncSettingsState(
                isUserSignedIn = true,
                isSyncEnabled = false,
                isLoading = false
            ),
            snackbarHostState = SnackbarHostState(),
            onNavigateBack = {},
            onSyncToggled = {},
            onSyncNowClicked = {}
        )
    }
}

@Preview(name = "Not Signed In", showBackground = true)
@Composable
private fun SyncSettingsScreenNotSignedInPreview() {
    TheDayToTheme {
        SyncSettingsScreen(
            uiState = SyncSettingsState(
                isUserSignedIn = false,
                isLoading = false
            ),
            snackbarHostState = SnackbarHostState(),
            onNavigateBack = {},
            onSyncToggled = {},
            onSyncNowClicked = {}
        )
    }
}

@Preview(name = "Syncing", showBackground = true)
@Composable
private fun SyncSettingsScreenSyncingPreview() {
    TheDayToTheme {
        SyncSettingsScreen(
            uiState = SyncSettingsState(
                isUserSignedIn = true,
                isSyncEnabled = true,
                syncState = SyncState.Syncing(0.5f),
                isLoading = false
            ),
            snackbarHostState = SnackbarHostState(),
            onNavigateBack = {},
            onSyncToggled = {},
            onSyncNowClicked = {}
        )
    }
}
