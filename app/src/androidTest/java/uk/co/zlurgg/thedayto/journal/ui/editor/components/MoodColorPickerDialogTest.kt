package uk.co.zlurgg.thedayto.journal.ui.editor.components

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTextInput
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import uk.co.zlurgg.thedayto.base.ComposeTest
import uk.co.zlurgg.thedayto.core.ui.theme.TheDayToTheme
import uk.co.zlurgg.thedayto.journal.domain.util.InputValidation

/**
 * UI tests for MoodColorPickerDialog.
 *
 * Tests cover:
 * - Empty mood shows error on save
 * - Valid mood triggers onSave callback
 * - Dismiss closes dialog via callback
 * - Character limit is enforced
 */
@RunWith(AndroidJUnit4::class)
class MoodColorPickerDialogTest : ComposeTest() {

    // ============================================================
    // Validation Tests
    // ============================================================

    @Test
    fun empty_mood_shows_error_on_save() {
        composeTestRule.setContent {
            TheDayToTheme {
                MoodColorPickerDialog(
                    showDialog = true,
                    onDismiss = {},
                    onSave = { _, _ -> }
                )
            }
        }

        // Dialog should be displayed
        composeTestRule
            .onNodeWithText("Create new mood color")
            .assertIsDisplayed()

        // Click save without entering mood
        composeTestRule
            .onNodeWithContentDescription("Save entry")
            .performClick()

        // Verify error message is displayed
        composeTestRule
            .onNodeWithText("Mood cannot be empty!")
            .assertIsDisplayed()
    }

    // ============================================================
    // Callback Tests
    // ============================================================

    @Test
    fun valid_mood_triggers_onSave() {
        var savedMood: String? = null
        var savedColor: String? = null

        composeTestRule.setContent {
            TheDayToTheme {
                MoodColorPickerDialog(
                    showDialog = true,
                    onDismiss = {},
                    onSave = { mood, color ->
                        savedMood = mood
                        savedColor = color
                    }
                )
            }
        }

        // Enter a mood name
        composeTestRule
            .onNodeWithText("Mood")
            .performTextInput("Excited")

        // Click save
        composeTestRule
            .onNodeWithContentDescription("Save entry")
            .performClick()

        // Verify callback received correct values
        assertEquals("Excited", savedMood)
        assertTrue("Color should not be null", savedColor != null)
    }

    @Test
    fun dismiss_calls_onDismiss() {
        var dismissCalled = false

        composeTestRule.setContent {
            TheDayToTheme {
                MoodColorPickerDialog(
                    showDialog = true,
                    onDismiss = { dismissCalled = true },
                    onSave = { _, _ -> }
                )
            }
        }

        // Click dismiss button (X icon)
        composeTestRule
            .onNodeWithContentDescription("Cancel")
            .performClick()

        // Verify callback was triggered
        assertTrue("onDismiss should be called", dismissCalled)
    }

    // ============================================================
    // Character Counter Tests
    // ============================================================

    @Test
    fun character_counter_displays_initial_count() {
        composeTestRule.setContent {
            TheDayToTheme {
                MoodColorPickerDialog(
                    showDialog = true,
                    onDismiss = {},
                    onSave = { _, _ -> }
                )
            }
        }

        // Verify initial counter shows 0/50
        composeTestRule
            .onNodeWithText("0/${InputValidation.MAX_MOOD_LENGTH}")
            .assertIsDisplayed()
    }
}
