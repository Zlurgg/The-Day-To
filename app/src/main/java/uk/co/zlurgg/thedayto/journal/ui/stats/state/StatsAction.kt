package uk.co.zlurgg.thedayto.journal.ui.stats.state

/**
 * User actions on the Stats screen
 */
sealed interface StatsAction {
    data object Refresh : StatsAction
    data object NavigateBack : StatsAction
}
