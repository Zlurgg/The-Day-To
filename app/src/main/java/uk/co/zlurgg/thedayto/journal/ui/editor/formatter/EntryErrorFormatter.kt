package uk.co.zlurgg.thedayto.journal.ui.editor.formatter

import android.content.Context
import uk.co.zlurgg.thedayto.R
import uk.co.zlurgg.thedayto.journal.domain.model.EntryError

/**
 * Formats [EntryError] into user-facing messages using string resources.
 *
 * Lives in the UI layer (not domain) because it depends on [Context].
 */
object EntryErrorFormatter {
    fun format(context: Context, error: EntryError): String = when (error) {
        EntryError.NotFound -> context.getString(R.string.error_entry_not_found)
        EntryError.LoadFailed -> context.getString(R.string.error_entry_load_failed)
        EntryError.DateLoadFailed -> context.getString(R.string.error_entry_date_load_failed)
        EntryError.NoMoodSelected -> context.getString(R.string.error_no_mood_selected)
        EntryError.SaveFailed -> context.getString(R.string.error_entry_save_failed)
        EntryError.RetryFailed -> context.getString(R.string.error_retry_failed)
    }
}
