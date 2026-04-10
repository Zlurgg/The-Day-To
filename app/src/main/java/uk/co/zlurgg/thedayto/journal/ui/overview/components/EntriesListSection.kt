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
import androidx.compose.material3.MaterialTheme
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
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import uk.co.zlurgg.thedayto.R
import uk.co.zlurgg.thedayto.core.domain.util.OrderType
import uk.co.zlurgg.thedayto.core.ui.components.JournalCard
import uk.co.zlurgg.thedayto.core.ui.theme.TheDayToTheme
import uk.co.zlurgg.thedayto.core.ui.theme.paddingMedium
import uk.co.zlurgg.thedayto.core.ui.theme.paddingSmall
import uk.co.zlurgg.thedayto.journal.domain.model.EntryWithMoodColor
import uk.co.zlurgg.thedayto.journal.domain.util.EntryOrder

/**
 * Section displaying the list of journal entries with sorting controls.
 *
 * Display logic:
 * - Current month, no entries, no today entry: prompt card only (no sort chips)
 * - Current month, has entries, no today entry: prompt card at top + sort chips + entries
 * - Has entries with today entry: sort chips + entries
 * - Past month, no entries: not rendered (caller hides the section)
 *
 * @param isLoading Disables swipe gestures during delete operations to prevent race conditions
 */
@Composable
fun EntriesListSection(
    entries: List<EntryWithMoodColor>,
    entryOrder: EntryOrder,
    onOrderChange: (EntryOrder) -> Unit,
    onEntryClick: (entryId: Int?) -> Unit,
    onDeleteEntry: (EntryWithMoodColor) -> Unit,
    isLoading: Boolean,
    isCurrentMonth: Boolean,
    hasTodayEntry: Boolean,
    entryPendingDelete: EntryWithMoodColor?,
    modifier: Modifier = Modifier,
    onCreateEntry: () -> Unit = {},
) {
    val haptic = LocalHapticFeedback.current
    val showPromptCard = isCurrentMonth && !hasTodayEntry

    JournalCard(modifier = modifier) {
        Column(modifier = Modifier.padding(paddingMedium)) {
            // Sort chips only when there are entries to sort
            if (entries.isNotEmpty()) {
                EntrySortSection(
                    modifier = Modifier.padding(vertical = paddingSmall),
                    entryOrder = entryOrder,
                    onOrderChange = onOrderChange,
                )
                Spacer(modifier = Modifier.height(paddingSmall))
            }

            // Prompt card for creating today's entry
            if (showPromptCard) {
                CreateEntryPromptCard(
                    onClick = onCreateEntry,
                )
                if (entries.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(paddingMedium))
                }
            }

            // Entry list
            if (entries.isNotEmpty()) {
                val localEntries = remember(entries) {
                    entries.toMutableStateList()
                }

                localEntries.forEach { entry ->
                    key(entry.id) {
                        val dismissState = rememberSwipeToDismissBoxState()

                        LaunchedEffect(dismissState.currentValue) {
                            if (dismissState.currentValue == SwipeToDismissBoxValue.EndToStart) {
                                onDeleteEntry(entry)
                            }
                        }

                        LaunchedEffect(entryPendingDelete) {
                            if (entryPendingDelete == null &&
                                dismissState.currentValue == SwipeToDismissBoxValue.EndToStart
                            ) {
                                dismissState.reset()
                            }
                        }

                        SwipeToDismissBox(
                            state = dismissState,
                            backgroundContent = {
                                val direction = dismissState.dismissDirection
                                if (direction == SwipeToDismissBoxValue.EndToStart) {
                                    Box(
                                        modifier = Modifier
                                            .fillMaxSize()
                                            .background(MaterialTheme.colorScheme.error)
                                            .padding(horizontal = paddingMedium),
                                        contentAlignment = Alignment.CenterEnd,
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Delete,
                                            contentDescription = stringResource(R.string.delete_entry),
                                            tint = MaterialTheme.colorScheme.onError,
                                        )
                                    }
                                }
                            },
                            enableDismissFromStartToEnd = false,
                            enableDismissFromEndToStart = !isLoading,
                        ) {
                            EntryItem(
                                entry = entry,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable {
                                        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                        onEntryClick(entry.id)
                                    },
                            )
                        }
                    }
                    Spacer(modifier = Modifier.height(paddingMedium))
                }
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
                    dateStamp = System.currentTimeMillis(),
                ),
                EntryWithMoodColor(
                    id = 2,
                    moodColorId = 2,
                    moodName = "Productive",
                    moodColor = "2196F3",
                    content = "Got a lot done today",
                    dateStamp = System.currentTimeMillis() - 86400000,
                ),
            ),
            entryOrder = EntryOrder.Date(OrderType.Descending),
            onOrderChange = {},
            onEntryClick = {},
            onDeleteEntry = {},
            isLoading = false,
            isCurrentMonth = true,
            hasTodayEntry = true,
            entryPendingDelete = null,
        )
    }
}

@Preview(name = "Prompt Card Only", showBackground = true)
@Composable
private fun EntriesListSectionPromptOnlyPreview() {
    TheDayToTheme {
        EntriesListSection(
            entries = emptyList(),
            entryOrder = EntryOrder.Date(OrderType.Descending),
            onOrderChange = {},
            onEntryClick = {},
            onDeleteEntry = {},
            isLoading = false,
            isCurrentMonth = true,
            hasTodayEntry = false,
            entryPendingDelete = null,
        )
    }
}

@Preview(name = "Prompt Card With Entries", showBackground = true)
@Composable
private fun EntriesListSectionPromptWithEntriesPreview() {
    TheDayToTheme {
        EntriesListSection(
            entries = listOf(
                EntryWithMoodColor(
                    id = 1,
                    moodColorId = 1,
                    moodName = "Happy",
                    moodColor = "4CAF50",
                    content = "Yesterday was great!",
                    dateStamp = System.currentTimeMillis() - 86400000,
                ),
            ),
            entryOrder = EntryOrder.Date(OrderType.Descending),
            onOrderChange = {},
            onEntryClick = {},
            onDeleteEntry = {},
            isLoading = false,
            isCurrentMonth = true,
            hasTodayEntry = false,
            entryPendingDelete = null,
        )
    }
}
