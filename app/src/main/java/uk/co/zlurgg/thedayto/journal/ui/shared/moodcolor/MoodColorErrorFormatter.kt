package uk.co.zlurgg.thedayto.journal.ui.shared.moodcolor

import androidx.annotation.StringRes
import uk.co.zlurgg.thedayto.R
import uk.co.zlurgg.thedayto.journal.domain.model.MoodColorError

/**
 * Maps [MoodColorError] variants to string resource IDs.
 *
 * Context-free: the Composable resolves the ID via `stringResource()` (for
 * persistent UI) or `resources.getString()` (for one-shot snackbars in
 * LaunchedEffect).
 */
object MoodColorErrorFormatter {
    @StringRes
    fun resourceId(error: MoodColorError): Int = when (error) {
        MoodColorError.BlankName -> R.string.error_blank_name
        MoodColorError.NameTooLong -> R.string.error_name_too_long
        MoodColorError.InvalidColor -> R.string.error_invalid_color
        MoodColorError.DuplicateName -> R.string.error_duplicate_name
        MoodColorError.LimitReached -> R.string.error_limit_reached
        MoodColorError.NotFound -> R.string.error_database
        MoodColorError.DatabaseError -> R.string.error_database
    }
}
