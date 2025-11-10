package uk.co.zlurgg.thedayto.journal.ui.stats

import android.content.res.Configuration
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material.icons.filled.Error
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import org.koin.androidx.compose.koinViewModel
import uk.co.zlurgg.thedayto.core.ui.theme.TheDayToTheme
import uk.co.zlurgg.thedayto.core.ui.theme.paddingMedium
import uk.co.zlurgg.thedayto.journal.ui.stats.components.MonthlyBreakdownCard
import uk.co.zlurgg.thedayto.journal.ui.stats.components.MoodDistributionCard
import uk.co.zlurgg.thedayto.journal.ui.stats.components.TotalStatsCard
import uk.co.zlurgg.thedayto.journal.ui.stats.state.StatsUiState
import java.time.LocalDate

/**
 * Root composable - handles ViewModel, state collection, and side effects
 */
@Composable
fun StatsScreenRoot(
    viewModel: StatsViewModel = koinViewModel(),
    onNavigateBack: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    StatsScreen(
        uiState = uiState,
        onNavigateBack = onNavigateBack
    )
}

/**
 * Presenter composable - pure UI with state and callbacks
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun StatsScreen(
    uiState: StatsUiState,
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Statistics") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back"
                        )
                    }
                }
            )
        },
        modifier = modifier
    ) { paddingValues ->
        when {
            uiState.error != null -> {
                ErrorStatsState(
                    message = uiState.error,
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues)
                )
            }
            uiState.isLoading -> {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            }
            uiState.isEmpty -> {
                EmptyStatsState(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues)
                )
            }
            else -> {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues)
                        .padding(horizontal = paddingMedium),
                    verticalArrangement = Arrangement.spacedBy(paddingMedium)
                ) {
                    item { Spacer(modifier = Modifier.height(0.dp)) } // Top spacing

                    item {
                        TotalStatsCard(
                            totalEntries = uiState.totalEntries,
                            firstEntryDate = uiState.firstEntryDate,
                            averageEntriesPerMonth = uiState.averageEntriesPerMonth
                        )
                    }

                    item {
                        MoodDistributionCard(
                            moodDistribution = uiState.moodDistribution
                        )
                    }

                    item {
                        MonthlyBreakdownCard(
                            monthlyBreakdown = uiState.monthlyBreakdown
                        )
                    }

                    item { Spacer(modifier = Modifier.height(0.dp)) } // Bottom spacing
                }
            }
        }
    }
}

@Composable
private fun ErrorStatsState(
    message: String,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.padding(StatsConstants.STATE_MESSAGE_PADDING),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            imageVector = Icons.Default.Error,
            contentDescription = null,
            modifier = Modifier.size(64.dp),
            tint = MaterialTheme.colorScheme.error
        )
        Spacer(Modifier.height(StatsConstants.STATE_MESSAGE_ICON_SPACING))
        Text(
            text = "Error",
            style = MaterialTheme.typography.headlineMedium
        )
        Spacer(Modifier.height(StatsConstants.STATE_MESSAGE_TEXT_SPACING))
        Text(
            text = message,
            style = MaterialTheme.typography.bodyLarge,
            textAlign = TextAlign.Center,
            color = MaterialTheme.colorScheme.error
        )
    }
}

@Composable
private fun EmptyStatsState(modifier: Modifier = Modifier) {
    Column(
        modifier = modifier.padding(StatsConstants.STATE_MESSAGE_PADDING),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            imageVector = Icons.Default.BarChart,
            contentDescription = null,
            modifier = Modifier.size(64.dp),
            tint = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(Modifier.height(StatsConstants.STATE_MESSAGE_ICON_SPACING))
        Text(
            text = "No stats yet",
            style = MaterialTheme.typography.headlineMedium
        )
        Spacer(Modifier.height(StatsConstants.STATE_MESSAGE_TEXT_SPACING))
        Text(
            text = "Start logging your mood to see your progress and insights!",
            style = MaterialTheme.typography.bodyLarge,
            textAlign = TextAlign.Center,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Preview(name = "Light Mode", showBackground = true)
@Preview(name = "Dark Mode", uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun StatsScreenPreview() {
    TheDayToTheme {
        StatsScreen(
            uiState = StatsUiState(
                totalEntries = 127,
                firstEntryDate = LocalDate.of(2024, 1, 15),
                averageEntriesPerMonth = 14.2f,
                moodDistribution = listOf(
                    StatsUiState.MoodCount("Happy", "#4CAF50", 34),
                    StatsUiState.MoodCount("Relaxed", "#2196F3", 28),
                    StatsUiState.MoodCount("Neutral", "#FFC107", 19)
                ),
                monthlyBreakdown = listOf(
                    StatsUiState.MonthStats("November", 2024, 11, 10, 33),
                    StatsUiState.MonthStats("October", 2024, 10, 23, 74),
                    StatsUiState.MonthStats("September", 2024, 9, 28, 93)
                ),
                isLoading = false,
                isEmpty = false
            ),
            onNavigateBack = {}
        )
    }
}

@Preview(name = "Empty State", showBackground = true)
@Composable
private fun StatsScreenEmptyPreview() {
    TheDayToTheme {
        StatsScreen(
            uiState = StatsUiState(isEmpty = true, isLoading = false),
            onNavigateBack = {}
        )
    }
}

@Preview(name = "Loading State", showBackground = true)
@Composable
private fun StatsScreenLoadingPreview() {
    TheDayToTheme {
        StatsScreen(
            uiState = StatsUiState(isLoading = true),
            onNavigateBack = {}
        )
    }
}
