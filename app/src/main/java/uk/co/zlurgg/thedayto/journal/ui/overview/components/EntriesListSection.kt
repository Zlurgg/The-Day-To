package uk.co.zlurgg.thedayto.journal.ui.overview.components

import android.content.res.Configuration
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.Icon
import androidx.compose.material3.SwipeToDismissBox
import androidx.compose.material3.SwipeToDismissBoxValue
import androidx.compose.material3.rememberSwipeToDismissBoxState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.key
import androidx.compose.runtime.remember
import androidx.compose.runtime.toMutableStateList
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import uk.co.zlurgg.thedayto.R
import uk.co.zlurgg.thedayto.core.domain.util.OrderType
import uk.co.zlurgg.thedayto.core.ui.theme.TheDayToTheme
import uk.co.zlurgg.thedayto.core.ui.theme.paddingMedium
import uk.co.zlurgg.thedayto.core.ui.theme.paddingSmall
import uk.co.zlurgg.thedayto.journal.domain.model.EntryWithMoodColor
import uk.co.zlurgg.thedayto.journal.domain.util.EntryOrder

/**
 * Section displaying the list of journal entries with sorting controls.
 * Shows empty state when no entries exist for the selected month.
 * Supports swipe-to-delete with undo functionality.
 *
 * @param isLoading Disables swipe gestures during delete/restore operations to prevent race conditions
 */
@Composable
fun EntriesListSection(
    entries: List<EntryWithMoodColor>,
    entryOrder: EntryOrder,
    onOrderChange: (EntryOrder) -> Unit,
    onEntryClick: (entryId: Int?) -> Unit,
    onDeleteEntry: (EntryWithMoodColor) -> Unit,
    isLoading: Boolean,
    modifier: Modifier = Modifier,
    onCreateEntry: () -> Unit = {},
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
            EmptyState(onCreateEntry = onCreateEntry)
        } else {
            // Create local mutable state list from Flow entries
            val localEntries = remember(entries) {
                entries.toMutableStateList()
            }

            // Sync local list with Flow updates
            LaunchedEffect(entries) {
                localEntries.clear()
                localEntries.addAll(entries)
            }

            // Render entries with swipe-to-delete
            localEntries.forEach { entry ->
                key(entry.id) {
                    val dismissState = rememberSwipeToDismissBoxState()

                    SwipeToDismissBox(
                        state = dismissState,
                        backgroundContent = {
                            // Only show background when actively swiping
                            val direction = dismissState.dismissDirection
                            if (direction == SwipeToDismissBoxValue.EndToStart) {
                                Box(
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .background(Color(0xFFD32F2F))  // Material Red 700
                                        .padding(horizontal = paddingMedium),
                                    contentAlignment = Alignment.CenterEnd
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Delete,
                                        contentDescription = stringResource(R.string.delete_entry),
                                        tint = Color.White
                                    )
                                }
                            }
                        },
                        enableDismissFromStartToEnd = false,
                        enableDismissFromEndToStart = !isLoading,  // Disable swipe during operations
                        onDismiss = { dismissDirection ->
                            // Modern Material 3 API - called when swipe completes
                            if (dismissDirection == SwipeToDismissBoxValue.EndToStart) {
                                // Remove from local list immediately (instant UI update)
                                localEntries.remove(entry)
                                // Trigger database deletion in background
                                onDeleteEntry(entry)
                            }
                        }
                    ) {
                        EntryItem(
                            entry = entry,
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    onEntryClick(entry.id)
                                }
                        )
                    }
                }
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
                EntryWithMoodColor(
                    id = 1,
                    moodColorId = 1,
                    moodName = "Happy",
                    moodColor = "4CAF50",
                    content = "Had a great day!",
                    dateStamp = System.currentTimeMillis()
                ),
                EntryWithMoodColor(
                    id = 2,
                    moodColorId = 2,
                    moodName = "Productive",
                    moodColor = "2196F3",
                    content = "Got a lot done today",
                    dateStamp = System.currentTimeMillis() - 86400000
                )
            ),
            entryOrder = EntryOrder.Date(OrderType.Descending),
            onOrderChange = {},
            onEntryClick = {},
            onDeleteEntry = {},
            isLoading = false
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
            onEntryClick = {},
            onDeleteEntry = {},
            isLoading = false
        )
    }
}
