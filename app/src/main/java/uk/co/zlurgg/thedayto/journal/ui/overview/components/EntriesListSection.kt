package uk.co.zlurgg.thedayto.journal.ui.overview.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import uk.co.zlurgg.thedayto.journal.domain.model.Entry
import uk.co.zlurgg.thedayto.journal.domain.util.EntryOrder
import uk.co.zlurgg.thedayto.core.ui.theme.paddingMedium
import uk.co.zlurgg.thedayto.core.ui.theme.paddingSmall

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
            LazyColumn(modifier = Modifier.fillMaxSize()) {
                items(
                    items = entries,
                    key = { it.id ?: 0 }
                ) { entry ->
                    AnimatedVisibility(
                        visible = true,
                        enter = fadeIn() + slideInVertically(
                            initialOffsetY = { it / 4 }
                        )
                    ) {
                        Column {
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
        }
    }
}
