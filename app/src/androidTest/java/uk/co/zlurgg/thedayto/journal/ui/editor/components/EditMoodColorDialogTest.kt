package uk.co.zlurgg.thedayto.journal.ui.editor.components

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTextClearance
import androidx.compose.ui.test.performTextInput
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import uk.co.zlurgg.thedayto.base.ComposeTest
import uk.co.zlurgg.thedayto.core.ui.theme.TheDayToTheme
import uk.co.zlurgg.thedayto.journal.domain.model.MoodColor
import uk.co.zlurgg.thedayto.journal.domain.util.InputValidation

/**
 * UI tests for EditMoodColorDialog.
 *
 * Tests cover:
 * - Dialog displays current mood name and color
 * - Mood name field is editable
 * - Character counter updates correctly
 * - Empty name validation shows error
 * - Save callback receives correct values
 * - Dismiss callback is triggered
 */
@RunWith(AndroidJUnit4::class)
class EditMoodColorDialogTest : ComposeTest() {

    private val testMoodColor = MoodColor(
        id = 1,
        mood = "Happy",
        color = "4CAF50",
        dateStamp = System.currentTimeMillis()
    )

    // ============================================================
    // Display Tests
    // ============================================================

    @Test
    fun dialog_displays_current_mood_name() {
        composeTestRule.setContent {
            TheDayToTheme {
                EditMoodColorDialog(
                    moodColor = testMoodColor,
                    showDialog = true,
                    onDismiss = {},
                    onSave = { _, _ -> }
                )
            }
        }

        // Verify dialog title is shown
        composeTestRule
            .onNodeWithText("Edit Mood Color")
            .assertIsDisplayed()

        // Verify mood name is displayed in text field
        composeTestRule
            .onNodeWithText("Happy")
            .assertIsDisplayed()
    }

    @Test
    fun dialog_displays_character_counter_with_current_length() {
        composeTestRule.setContent {
            TheDayToTheme {
                EditMoodColorDialog(
                    moodColor = testMoodColor,
                    showDialog = true,
                    onDismiss = {},
                    onSave = { _, _ -> }
                )
            }
        }

        // Verify character counter shows current mood length
        // "Happy" = 5 characters
        composeTestRule
            .onNodeWithText("5/${InputValidation.MAX_MOOD_LENGTH}")
            .assertIsDisplayed()
    }

    // ============================================================
    // Edit Tests
    // ============================================================

    @Test
    fun mood_name_field_is_editable() {
        composeTestRule.setContent {
            TheDayToTheme {
                EditMoodColorDialog(
                    moodColor = testMoodColor,
                    showDialog = true,
                    onDismiss = {},
                    onSave = { _, _ -> }
                )
            }
        }

        // Clear existing text and type new value
        composeTestRule
            .onNodeWithText("Happy")
            .performTextClearance()

        composeTestRule
            .onNodeWithText("Mood")
            .performTextInput("Excited")

        // Verify new text is displayed
        composeTestRule
            .onNodeWithText("Excited")
            .assertIsDisplayed()
    }

    @Test
    fun character_counter_updates_when_typing() {
        composeTestRule.setContent {
            TheDayToTheme {
                EditMoodColorDialog(
                    moodColor = testMoodColor,
                    showDialog = true,
                    onDismiss = {},
                    onSave = { _, _ -> }
                )
            }
        }

        // Initial counter: "Happy" = 5 chars
        composeTestRule
            .onNodeWithText("5/${InputValidation.MAX_MOOD_LENGTH}")
            .assertIsDisplayed()

        // Clear and type longer text
        composeTestRule
            .onNodeWithText("Happy")
            .performTextClearance()

        composeTestRule
            .onNodeWithText("Mood")
            .performTextInput("Very Excited")

        // Verify counter updated: "Very Excited" = 12 chars
        composeTestRule
            .onNodeWithText("12/${InputValidation.MAX_MOOD_LENGTH}")
            .assertIsDisplayed()
    }

    // ============================================================
    // Validation Tests
    // ============================================================

    @Test
    fun empty_name_shows_error_on_save() {
        composeTestRule.setContent {
            TheDayToTheme {
                EditMoodColorDialog(
                    moodColor = testMoodColor,
                    showDialog = true,
                    onDismiss = {},
                    onSave = { _, _ -> }
                )
            }
        }

        // Clear the mood name
        composeTestRule
            .onNodeWithText("Happy")
            .performTextClearance()

        // Click save button
        composeTestRule
            .onNodeWithContentDescription("Save")
            .performClick()

        // Verify error message is displayed
        composeTestRule
            .onNodeWithText("Mood name cannot be empty")
            .assertIsDisplayed()
    }

    // ============================================================
    // Callback Tests
    // ============================================================

    @Test
    fun save_calls_onSave_with_new_values() {
        var savedMood: String? = null
        var savedColor: String? = null

        composeTestRule.setContent {
            TheDayToTheme {
                EditMoodColorDialog(
                    moodColor = testMoodColor,
                    showDialog = true,
                    onDismiss = {},
                    onSave = { mood, color ->
                        savedMood = mood
                        savedColor = color
                    }
                )
            }
        }

        // Edit the mood name
        composeTestRule
            .onNodeWithText("Happy")
            .performTextClearance()

        composeTestRule
            .onNodeWithText("Mood")
            .performTextInput("Joyful")

        // Click save
        composeTestRule
            .onNodeWithContentDescription("Save")
            .performClick()

        // Verify callback received correct values
        assertEquals("Joyful", savedMood)
        // Color should be set (either original or picker default)
        assertTrue("Color should not be null", savedColor != null)
    }

    @Test
    fun dismiss_calls_onDismiss() {
        var dismissCalled = false

        composeTestRule.setContent {
            TheDayToTheme {
                EditMoodColorDialog(
                    moodColor = testMoodColor,
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
}
