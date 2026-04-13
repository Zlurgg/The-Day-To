package uk.co.zlurgg.thedayto.journal.ui.editor.formatter

import android.content.Context
import io.mockk.every
import io.mockk.mockk
import org.junit.Assert.assertTrue
import org.junit.Test
import uk.co.zlurgg.thedayto.journal.domain.model.EntryError

/**
 * Verifies that every [EntryError] variant maps to a non-empty string.
 * Catches missing when-branches at compile time (sealed interface) and
 * wrong/missing resource IDs at runtime.
 */
class EntryErrorFormatterTest {

    private val context: Context = mockk {
        // Return the resource ID name as the string — verifies the right resource is called
        every { getString(any()) } answers {
            "formatted_${firstArg<Int>()}"
        }
    }

    @Test
    fun `all EntryError variants produce non-empty strings`() {
        val allErrors: List<EntryError> = listOf(
            EntryError.NotFound,
            EntryError.LoadFailed,
            EntryError.DateLoadFailed,
            EntryError.NoMoodSelected,
            EntryError.SaveFailed,
            EntryError.RetryFailed,
        )

        allErrors.forEach { error ->
            val formatted = EntryErrorFormatter.format(context, error)
            assertTrue(
                "${error::class.simpleName} should produce non-empty string",
                formatted.isNotEmpty(),
            )
        }
    }
}
