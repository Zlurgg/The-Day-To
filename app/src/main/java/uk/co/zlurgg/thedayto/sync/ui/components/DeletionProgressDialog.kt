package uk.co.zlurgg.thedayto.sync.ui.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import uk.co.zlurgg.thedayto.R
import uk.co.zlurgg.thedayto.auth.domain.usecases.DeletionProgress

/**
 * Dialog showing deletion progress.
 *
 * Displays the current step of the deletion process with a progress indicator.
 */
@Composable
fun DeletionProgressDialog(
    progress: DeletionProgress,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier,
) {
    AlertDialog(
        onDismissRequest = { /* Non-dismissible during deletion */ },
        title = {
            Text(text = stringResource(R.string.delete_account_progress_title))
        },
        text = {
            Column {
                when (progress) {
                    is DeletionProgress.Starting -> {
                        ProgressStep(
                            text = stringResource(R.string.delete_account_progress_starting),
                            isInProgress = true,
                        )
                    }

                    is DeletionProgress.CancellingSync -> {
                        ProgressStep(
                            text = stringResource(R.string.delete_account_progress_cancelling_sync),
                            isInProgress = true,
                        )
                    }

                    is DeletionProgress.DeletingRemote -> {
                        ProgressStep(
                            text = stringResource(R.string.delete_account_progress_deleting_remote),
                            isInProgress = true,
                        )
                    }

                    is DeletionProgress.DeletingAccount -> {
                        ProgressStep(
                            text = stringResource(R.string.delete_account_progress_deleting_account),
                            isInProgress = true,
                        )
                    }

                    is DeletionProgress.ClearingLocal -> {
                        ProgressStep(
                            text = stringResource(R.string.delete_account_progress_clearing_local),
                            isInProgress = true,
                        )
                    }

                    is DeletionProgress.Complete -> {
                        ProgressStep(
                            text = stringResource(R.string.delete_account_progress_complete),
                            isComplete = true,
                        )
                    }

                    is DeletionProgress.RequiresReAuth,
                    is DeletionProgress.Failed,
                        -> {
                        // These are handled outside this dialog
                    }
                }
            }
        },
        confirmButton = {
            if (progress is DeletionProgress.Complete) {
                TextButton(onClick = onDismiss) {
                    Text(stringResource(R.string.ok))
                }
            }
        },
        modifier = modifier,
    )
}

@Composable
private fun ProgressStep(
    text: String,
    isInProgress: Boolean = false,
    isComplete: Boolean = false,
    modifier: Modifier = Modifier,
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = modifier,
    ) {
        when {
            isInProgress -> {
                CircularProgressIndicator(
                    modifier = Modifier.size(20.dp),
                    strokeWidth = 2.dp,
                )
            }

            isComplete -> {
                Icon(
                    imageVector = Icons.Default.CheckCircle,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(20.dp),
                )
            }
        }
        Spacer(modifier = Modifier.width(12.dp))
        Text(text = text, style = MaterialTheme.typography.bodyMedium)
    }
}
