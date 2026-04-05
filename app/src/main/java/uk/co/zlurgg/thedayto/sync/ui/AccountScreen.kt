package uk.co.zlurgg.thedayto.sync.ui

import android.content.res.Configuration
import androidx.activity.compose.LocalActivity
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
import androidx.compose.material.icons.filled.CloudSync
import androidx.compose.material.icons.filled.Error
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Sync
import androidx.compose.material.icons.filled.DeleteForever
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
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
import org.koin.compose.koinInject
import uk.co.zlurgg.thedayto.R
import uk.co.zlurgg.thedayto.auth.domain.usecases.DeletionProgress
import uk.co.zlurgg.thedayto.auth.ui.CredentialProviderFactory
import uk.co.zlurgg.thedayto.auth.ui.components.DevSignInButton
import uk.co.zlurgg.thedayto.auth.ui.components.SignInButton
import uk.co.zlurgg.thedayto.core.ui.theme.TheDayToTheme
import uk.co.zlurgg.thedayto.core.ui.theme.paddingMedium
import uk.co.zlurgg.thedayto.core.ui.theme.paddingSmall
import uk.co.zlurgg.thedayto.sync.domain.model.SyncResult
import uk.co.zlurgg.thedayto.sync.domain.model.SyncState
import uk.co.zlurgg.thedayto.sync.ui.components.DeleteAccountConfirmDialog
import uk.co.zlurgg.thedayto.sync.ui.components.DeletionProgressDialog
import java.text.DateFormat
import java.util.Date

/**
 * Root composable - handles ViewModel, state collection, and side effects
 */
@Composable
fun AccountScreenRoot(
    viewModel: SyncSettingsViewModel = koinViewModel(),
    credentialProviderFactory: CredentialProviderFactory = koinInject(),
    onNavigateBack: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }

    // Get Activity for credential manager
    val activity = LocalActivity.current
    val serverClientId = stringResource(R.string.web_client_id)

    LaunchedEffect(uiState.error) {
        uiState.error?.let { error ->
            snackbarHostState.showSnackbar(error)
            viewModel.onErrorDismissed()
        }
    }

    AccountScreen(
        uiState = uiState,
        snackbarHostState = snackbarHostState,
        onNavigateBack = onNavigateBack,
        onSyncNowClicked = viewModel::onSyncNowClicked,
        onSignInClick = {
            activity?.let { act ->
                viewModel.signIn(credentialProviderFactory.create(act, serverClientId))
            }
        },
        onDevSignInClick = viewModel::devSignIn,
        onSignOutClick = viewModel::signOut,
        onDeleteAccountClick = viewModel::onDeleteAccountRequested,
        onDeleteAccountConfirm = viewModel::onDeleteAccountConfirmed,
        onDeleteAccountCancel = viewModel::onDeleteAccountCancelled,
        onDeletionProgressDismiss = viewModel::onDeletionProgressDismissed,
        onReAuthConfirm = {
            activity?.let { act ->
                viewModel.onReAuthCompleted(credentialProviderFactory.create(act, serverClientId))
            }
        },
        onReAuthDismiss = viewModel::onReAuthDismissed
    )
}

/**
 * Presenter composable - pure UI with state and callbacks
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun AccountScreen(
    uiState: SyncSettingsState,
    snackbarHostState: SnackbarHostState,
    onNavigateBack: () -> Unit,
    onSyncNowClicked: () -> Unit,
    onSignInClick: () -> Unit,
    onDevSignInClick: () -> Unit,
    onSignOutClick: () -> Unit,
    onDeleteAccountClick: () -> Unit,
    onDeleteAccountConfirm: () -> Unit,
    onDeleteAccountCancel: () -> Unit,
    onDeletionProgressDismiss: () -> Unit,
    onReAuthConfirm: () -> Unit,
    onReAuthDismiss: () -> Unit,
    modifier: Modifier = Modifier
) {
    // Delete Account Confirmation Dialog
    if (uiState.showDeleteConfirmDialog) {
        DeleteAccountConfirmDialog(
            onConfirm = onDeleteAccountConfirm,
            onDismiss = onDeleteAccountCancel
        )
    }

    // Deletion Progress Dialog
    uiState.deletionProgress?.let { progress ->
        if (progress !is DeletionProgress.RequiresReAuth && progress !is DeletionProgress.Failed) {
            DeletionProgressDialog(
                progress = progress,
                onDismiss = onDeletionProgressDismiss
            )
        }
    }

    // Re-authentication Dialog
    if (uiState.showReAuthDialog) {
        AlertDialog(
            onDismissRequest = onReAuthDismiss,
            title = { Text(stringResource(R.string.delete_account_reauth_title)) },
            text = { Text(stringResource(R.string.delete_account_reauth_message)) },
            confirmButton = {
                Button(onClick = onReAuthConfirm) {
                    Text(stringResource(R.string.sign_in))
                }
            },
            dismissButton = {
                TextButton(onClick = onReAuthDismiss) {
                    Text(stringResource(R.string.cancel))
                }
            }
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.account_title)) },
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
            uiState.isLoading && !uiState.isUserSignedIn && !uiState.isSigningIn -> {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            }
            else -> {
                AccountContent(
                    uiState = uiState,
                    onSyncNowClicked = onSyncNowClicked,
                    onSignInClick = onSignInClick,
                    onDevSignInClick = onDevSignInClick,
                    onSignOutClick = onSignOutClick,
                    onDeleteAccountClick = onDeleteAccountClick,
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues)
                        .verticalScroll(rememberScrollState())
                )
            }
        }
    }
}

/**
 * Account section showing sign-in/sign-out state
 */
@Composable
private fun AccountSection(
    isSignedIn: Boolean,
    userEmail: String?,
    isSigningIn: Boolean,
    isDevSignInAvailable: Boolean,
    onSignInClick: () -> Unit,
    onDevSignInClick: () -> Unit,
    onSignOutClick: () -> Unit,
    onDeleteAccountClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainerLow
        )
    ) {
        if (isSignedIn) {
            // Signed in state
            Column(modifier = Modifier.fillMaxWidth()) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(paddingMedium),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.weight(1f)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Person,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary
                        )
                        Spacer(modifier = Modifier.width(paddingMedium))
                        Column {
                            Text(
                                text = userEmail ?: stringResource(R.string.sync_signed_in),
                                style = MaterialTheme.typography.titleMedium
                            )
                            Text(
                                text = stringResource(R.string.sync_cloud_available),
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                    TextButton(onClick = onSignOutClick) {
                        Text(
                            text = stringResource(R.string.sign_out),
                            color = MaterialTheme.colorScheme.error
                        )
                    }
                }

                // Danger Zone - Delete Account
                HorizontalDivider(modifier = Modifier.padding(horizontal = paddingMedium))
                TextButton(
                    onClick = onDeleteAccountClick,
                    modifier = Modifier.padding(horizontal = paddingSmall),
                    colors = ButtonDefaults.textButtonColors(
                        contentColor = MaterialTheme.colorScheme.error
                    )
                ) {
                    Icon(
                        imageVector = Icons.Default.DeleteForever,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(paddingSmall))
                    Text(stringResource(R.string.delete_account_label))
                }
            }
        } else {
            // Not signed in state
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(paddingMedium),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Icon(
                    imageVector = Icons.Default.CloudSync,
                    contentDescription = null,
                    modifier = Modifier.size(48.dp),
                    tint = MaterialTheme.colorScheme.primary
                )
                Spacer(modifier = Modifier.height(paddingMedium))
                Text(
                    text = stringResource(R.string.sync_sign_in_to_enable),
                    style = MaterialTheme.typography.titleMedium
                )
                Spacer(modifier = Modifier.height(paddingSmall))
                Text(
                    text = stringResource(R.string.sync_sign_in_description),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(paddingMedium))
                if (isSigningIn) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(48.dp)
                    )
                } else {
                    SignInButton(
                        onClick = onSignInClick,
                        showButton = true
                    )
                    if (isDevSignInAvailable) {
                        DevSignInButton(
                            onClick = onDevSignInClick,
                            showButton = true
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun AccountContent(
    uiState: SyncSettingsState,
    onSyncNowClicked: () -> Unit,
    onSignInClick: () -> Unit,
    onDevSignInClick: () -> Unit,
    onSignOutClick: () -> Unit,
    onDeleteAccountClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.padding(paddingMedium),
        verticalArrangement = Arrangement.spacedBy(paddingMedium)
    ) {
        // Account Section (sign-in/sign-out)
        AccountSection(
            isSignedIn = uiState.isUserSignedIn,
            userEmail = uiState.userEmail,
            isSigningIn = uiState.isSigningIn,
            isDevSignInAvailable = uiState.isDevSignInAvailable,
            onSignInClick = onSignInClick,
            onDevSignInClick = onDevSignInClick,
            onSignOutClick = onSignOutClick,
            onDeleteAccountClick = onDeleteAccountClick
        )

        // Show sync status when signed in (sync is auto-enabled)
        if (uiState.isUserSignedIn) {
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
                    text = stringResource(R.string.sync_status_success),
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
private fun AccountScreenPreview() {
    TheDayToTheme {
        AccountScreen(
            uiState = SyncSettingsState(
                isUserSignedIn = true,
                userEmail = "user@example.com",
                syncState = SyncState.Success(SyncResult(2, 1, 3, 0)),
                lastSyncTimestamp = System.currentTimeMillis(),
                isLoading = false
            ),
            snackbarHostState = SnackbarHostState(),
            onNavigateBack = {},
            onSyncNowClicked = {},
            onSignInClick = {},
            onDevSignInClick = {},
            onSignOutClick = {},
            onDeleteAccountClick = {},
            onDeleteAccountConfirm = {},
            onDeleteAccountCancel = {},
            onDeletionProgressDismiss = {},
            onReAuthConfirm = {},
            onReAuthDismiss = {}
        )
    }
}

@Preview(name = "Not Signed In", showBackground = true)
@Composable
private fun AccountScreenNotSignedInPreview() {
    TheDayToTheme {
        AccountScreen(
            uiState = SyncSettingsState(
                isUserSignedIn = false,
                isDevSignInAvailable = true,
                isLoading = false
            ),
            snackbarHostState = SnackbarHostState(),
            onNavigateBack = {},
            onSyncNowClicked = {},
            onSignInClick = {},
            onDevSignInClick = {},
            onSignOutClick = {},
            onDeleteAccountClick = {},
            onDeleteAccountConfirm = {},
            onDeleteAccountCancel = {},
            onDeletionProgressDismiss = {},
            onReAuthConfirm = {},
            onReAuthDismiss = {}
        )
    }
}

@Preview(name = "Syncing", showBackground = true)
@Composable
private fun AccountScreenSyncingPreview() {
    TheDayToTheme {
        AccountScreen(
            uiState = SyncSettingsState(
                isUserSignedIn = true,
                userEmail = "user@example.com",
                syncState = SyncState.Syncing(0.5f),
                isLoading = false
            ),
            snackbarHostState = SnackbarHostState(),
            onNavigateBack = {},
            onSyncNowClicked = {},
            onSignInClick = {},
            onDevSignInClick = {},
            onSignOutClick = {},
            onDeleteAccountClick = {},
            onDeleteAccountConfirm = {},
            onDeleteAccountCancel = {},
            onDeletionProgressDismiss = {},
            onReAuthConfirm = {},
            onReAuthDismiss = {}
        )
    }
}
