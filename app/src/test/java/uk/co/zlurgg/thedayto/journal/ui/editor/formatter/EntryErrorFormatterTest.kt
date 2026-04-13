package uk.co.zlurgg.thedayto.journal.ui.editor.formatter

import org.junit.Assert.assertNotEquals
import org.junit.Test
import uk.co.zlurgg.thedayto.journal.domain.model.EntryError

/**
 * Verifies that every [EntryError] variant maps to a valid resource ID.
 * Catches missing when-branches at compile time (sealed interface) and
 * ensures no variant maps to 0 (invalid resource) at runtime.
 *
 * No Context or mockk needed — the formatter is now a pure Error → Int mapper.
 */
class EntryErrorFormatterTest {

    @Test
    fun `all EntryError variants produce valid resource IDs`() {
        val allErrors: List<EntryError> = listOf(
            EntryError.NotFound,
            EntryError.LoadFailed,
            EntryError.DateLoadFailed,
            EntryError.NoMoodSelected,
            EntryError.SaveFailed,
            EntryError.RetryFailed,
        )

        allErrors.forEach { error ->
            val resId = EntryErrorFormatter.resourceId(error)
            assertNotEquals(
                "${error::class.simpleName} should produce a valid resource ID",
                0,
                resId,
            )
        }
    }
}
