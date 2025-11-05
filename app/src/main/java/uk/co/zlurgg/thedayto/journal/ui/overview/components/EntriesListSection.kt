package uk.co.zlurgg.thedayto.journal.ui.overview.components

import android.content.res.Configuration
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import uk.co.zlurgg.thedayto.core.domain.util.OrderType
import uk.co.zlurgg.thedayto.core.ui.theme.TheDayToTheme
import uk.co.zlurgg.thedayto.core.ui.theme.paddingMedium
import uk.co.zlurgg.thedayto.core.ui.theme.paddingSmall
import uk.co.zlurgg.thedayto.journal.domain.model.Entry
import uk.co.zlurgg.thedayto.journal.domain.util.EntryOrder

/**
 * Section displaying the list of journal entries with sorting controls.
 * Shows empty state when no entries exist for the selected month.
 */
@Composable
fun EntriesListSection(
    entries: List<Entry>,
    entryOrder: EntryOrder,
    onOrderChange: (EntryOrder) -> Unit,
    onEntryClick: (entryId: Int?) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier) {
        // Entry sorting controls
        EntrySortSection(
            modifier = Modifier.padding(vertical = paddingSmall),
            entryOrder = entryOrder,
            onOrderChange = onOrderChange
        )

        Spacer(modifier = Modifier.height(paddingSmall))

        // Show empty state if no entries, otherwise show list
        if (entries.isEmpty()) {
            EmptyState()
        } else {
            // Render entries in Column (parent screen is scrollable)
            entries.forEach { entry ->
                EntryItem(
                    entry = entry,
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable {
                            onEntryClick(entry.id)
                        }
                )
                Spacer(modifier = Modifier.height(paddingMedium))
            }
        }
    }
}

@Preview(name = "Light Mode - With Entries", showBackground = true)
@Preview(name = "Dark Mode - With Entries", uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun EntriesListSectionPreview() {
    TheDayToTheme {
        EntriesListSection(
            entries = listOf(
                Entry(
                    mood = "Happy",
                    content = "Had a great day!",
                    dateStamp = System.currentTimeMillis(),
                    color = "#4CAF50",
                    id = 1
                ),
                Entry(
                    mood = "Productive",
                    content = "Got a lot done today",
                    dateStamp = System.currentTimeMillis() - 86400000,
                    color = "#2196F3",
                    id = 2
                )
            ),
            entryOrder = EntryOrder.Date(OrderType.Descending),
            onOrderChange = {},
            onEntryClick = {}
        )
    }
}

@Preview(name = "Empty State", showBackground = true)
@Composable
private fun EntriesListSectionEmptyPreview() {
    TheDayToTheme {
        EntriesListSection(
            entries = emptyList(),
            entryOrder = EntryOrder.Date(OrderType.Descending),
            onOrderChange = {},
            onEntryClick = {}
        )
    }
}
