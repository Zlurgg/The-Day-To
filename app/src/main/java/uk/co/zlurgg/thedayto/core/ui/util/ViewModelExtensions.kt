package uk.co.zlurgg.thedayto.core.ui.util

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import uk.co.zlurgg.thedayto.journal.ui.overview.util.TimeConstants

/**
 * Creates a debounced loading job that only shows loading state if operation takes longer than threshold.
 *
 * This prevents flickering loading indicators for fast operations while still providing feedback
 * for longer operations.
 *
 * **Usage:**
 * ```kotlin
 * val loadingJob = launchDebouncedLoading { isLoading ->
 *     _uiState.update { it.copy(isLoading = isLoading) }
 * }
 *
 * try {
 *     // ... perform operation ...
 *     loadingJob.cancel()  // Cancel if finished quickly
 *     updateLoadingState(false)
 * } catch (e: Exception) {
 *     loadingJob.cancel()
 *     updateLoadingState(false)
 * }
 * ```
 *
 * @param delayMs Delay before showing loading state (default: 150ms from TimeConstants)
 * @param updateLoadingState Callback to update loading state in UI (typically updates StateFlow)
 * @return Job that can be cancelled when operation completes
 */
fun CoroutineScope.launchDebouncedLoading(
    delayMs: Long = TimeConstants.LOADING_DEBOUNCE_MS,
    updateLoadingState: (Boolean) -> Unit
): Job {
    return launch {
        delay(delayMs)
        updateLoadingState(true)
    }
}
