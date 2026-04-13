package uk.co.zlurgg.thedayto.journal.ui.editor.formatter

import androidx.annotation.StringRes
import uk.co.zlurgg.thedayto.R
import uk.co.zlurgg.thedayto.journal.domain.model.EntryError

/**
 * Maps [EntryError] variants to string resource IDs.
 *
 * Context-free: the Composable resolves the ID via `stringResource()` (for
 * persistent UI) or `resources.getString()` (for one-shot snackbars in
 * LaunchedEffect). This avoids the `LocalContextGetResourceValueCall` lint
 * issue and ensures config-change correctness.
 */
object EntryErrorFormatter {
    @StringRes
    fun resourceId(error: EntryError): Int = when (error) {
        EntryError.NotFound -> R.string.error_entry_not_found
        EntryError.LoadFailed -> R.string.error_entry_load_failed
        EntryError.DateLoadFailed -> R.string.error_entry_date_load_failed
        EntryError.NoMoodSelected -> R.string.error_no_mood_selected
        EntryError.SaveFailed -> R.string.error_entry_save_failed
        EntryError.RetryFailed -> R.string.error_retry_failed
    }
}
