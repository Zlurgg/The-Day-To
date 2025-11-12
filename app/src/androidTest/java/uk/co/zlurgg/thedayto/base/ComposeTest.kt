package uk.co.zlurgg.thedayto.base

import androidx.compose.ui.test.junit4.createComposeRule
import org.junit.Rule
import org.koin.test.KoinTest

/**
 * Base class for Compose UI instrumented tests.
 *
 * Provides:
 * - ComposeTestRule for UI testing
 * - Koin test support for DI
 * - Helper functions for common assertions
 *
 * Usage:
 * ```kotlin
 * class MyScreenTest : ComposeTest() {
 *     @Test
 *     fun myTest() {
 *         composeTestRule.setContent {
 *             MyScreen()
 *         }
 *         // Use composeTestRule matchers
 *     }
 * }
 * ```
 */
abstract class ComposeTest : KoinTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    /**
     * Helper function to wait for a condition to be true.
     * Useful for waiting for async operations to complete.
     */
    protected fun waitUntil(
        timeoutMillis: Long = 5000L,
        condition: () -> Boolean
    ) {
        composeTestRule.waitUntil(timeoutMillis) {
            condition()
        }
    }
}
