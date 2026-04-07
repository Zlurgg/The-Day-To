package uk.co.zlurgg.thedayto.journal.domain.model

import android.content.Context
import uk.co.zlurgg.thedayto.R

/**
 * Formats MoodColorError into user-friendly messages.
 */
object MoodColorErrorFormatter {
    fun format(context: Context, error: MoodColorError): String = when (error) {
        MoodColorError.BlankName -> context.getString(R.string.error_blank_name)
        MoodColorError.NameTooLong -> context.getString(R.string.error_name_too_long)
        MoodColorError.InvalidColor -> context.getString(R.string.error_invalid_color)
        MoodColorError.DuplicateName -> context.getString(R.string.error_duplicate_name)
        MoodColorError.LimitReached -> context.getString(R.string.error_limit_reached)
        MoodColorError.NotFound -> context.getString(R.string.error_database)
        MoodColorError.DatabaseError -> context.getString(R.string.error_database)
    }
}
