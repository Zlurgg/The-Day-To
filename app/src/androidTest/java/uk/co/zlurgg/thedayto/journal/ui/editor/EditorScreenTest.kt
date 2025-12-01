package uk.co.zlurgg.thedayto.journal.ui.editor

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTextInput
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import uk.co.zlurgg.thedayto.base.ComposeTest
import uk.co.zlurgg.thedayto.core.ui.theme.TheDayToTheme
import uk.co.zlurgg.thedayto.journal.domain.model.MoodColor
import uk.co.zlurgg.thedayto.journal.ui.editor.components.ContentItem
import uk.co.zlurgg.thedayto.journal.ui.editor.components.MoodItem

/**
 * UI tests for Editor screen components.
 *
 * Tests the public sub-components of the Editor screen:
 * - MoodItem: Mood selection dropdown and add button
 * - ContentItem: Content text field
 *
 * Note: EditorScreen is a private composable, so we test
 * its public sub-components directly following the presenter pattern.
 */
@RunWith(AndroidJUnit4::class)
class EditorScreenTest : ComposeTest() {

    private val testMoodColors = listOf(
        MoodColor(id = 1, mood = "Happy", color = "4CAF50", dateStamp = System.currentTimeMillis()),
        MoodColor(id = 2, mood = "Sad", color = "2196F3", dateStamp = System.currentTimeMillis()),
        MoodColor(id = 3, mood = "Calm", color = "9C27B0", dateStamp = System.currentTimeMillis())
    )

    // ============================================================
    // MoodItem Tests
    // ============================================================

    @Test
    fun mood_selection_displays_options_when_dropdown_clicked() {
        composeTestRule.setContent {
            TheDayToTheme {
                MoodItem(
                    selectedMoodColorId = null,
                    moodColors = testMoodColors,
                    hint = "How're you feeling today?",
                    showMoodColorDialog = false,
                    onMoodSelected = {},
                    onDeleteMoodColor = {},
                    onEditMoodColor = {},
                    onToggleMoodColorDialog = {},
                    onSaveMoodColor = { _, _ -> }
                )
            }
        }

        // Click on the dropdown to expand it
        composeTestRule
            .onNodeWithText("How're you feeling today?")
            .performClick()

        // Verify mood options are displayed
        composeTestRule
            .onNodeWithText("Happy")
            .assertIsDisplayed()

        composeTestRule
            .onNodeWithText("Sad")
            .assertIsDisplayed()

        composeTestRule
            .onNodeWithText("Calm")
            .assertIsDisplayed()
    }

    @Test
    fun mood_selection_triggers_callback_when_mood_selected() {
        var selectedMoodId: Int? = null

        composeTestRule.setContent {
            TheDayToTheme {
                MoodItem(
                    selectedMoodColorId = null,
                    moodColors = testMoodColors,
                    hint = "How're you feeling today?",
                    showMoodColorDialog = false,
                    onMoodSelected = { selectedMoodId = it },
                    onDeleteMoodColor = {},
                    onEditMoodColor = {},
                    onToggleMoodColorDialog = {},
                    onSaveMoodColor = { _, _ -> }
                )
            }
        }

        // Click on the dropdown to expand it
        composeTestRule
            .onNodeWithText("How're you feeling today?")
            .performClick()

        // Select a mood
        composeTestRule
            .onNodeWithText("Happy")
            .performClick()

        // Verify callback received correct ID
        assertEquals(1, selectedMoodId)
    }

    @Test
    fun add_mood_button_triggers_toggle_callback() {
        var toggleCalled = false

        composeTestRule.setContent {
            TheDayToTheme {
                MoodItem(
                    selectedMoodColorId = null,
                    moodColors = testMoodColors,
                    hint = "How're you feeling today?",
                    showMoodColorDialog = false,
                    onMoodSelected = {},
                    onDeleteMoodColor = {},
                    onEditMoodColor = {},
                    onToggleMoodColorDialog = { toggleCalled = true },
                    onSaveMoodColor = { _, _ -> }
                )
            }
        }

        // Click on the color wheel add button (always visible)
        composeTestRule
            .onNodeWithContentDescription("add custom mood color")
            .performClick()

        // Verify callback was triggered
        assertTrue("onToggleMoodColorDialog should be called", toggleCalled)
    }

    @Test
    fun add_mood_button_in_dropdown_triggers_toggle_callback() {
        var toggleCalled = false

        composeTestRule.setContent {
            TheDayToTheme {
                MoodItem(
                    selectedMoodColorId = null,
                    moodColors = testMoodColors,
                    hint = "How're you feeling today?",
                    showMoodColorDialog = false,
                    onMoodSelected = {},
                    onDeleteMoodColor = {},
                    onEditMoodColor = {},
                    onToggleMoodColorDialog = { toggleCalled = true },
                    onSaveMoodColor = { _, _ -> }
                )
            }
        }

        // Open dropdown
        composeTestRule
            .onNodeWithText("How're you feeling today?")
            .performClick()

        // Click the "add custom mood color" option in dropdown
        composeTestRule
            .onNodeWithText("add custom mood color")
            .performClick()

        // Verify callback was triggered
        assertTrue("onToggleMoodColorDialog should be called", toggleCalled)
    }

    // ============================================================
    // ContentItem Tests
    // ============================================================

    @Test
    fun content_field_accepts_input() {
        var enteredContent = ""

        composeTestRule.setContent {
            TheDayToTheme {
                ContentItem(
                    content = enteredContent,
                    hint = "Any additional info?",
                    isHintVisible = true,
                    onContentChange = { enteredContent = it },
                    onFocusChange = {}
                )
            }
        }

        // Click on the field and type text
        composeTestRule
            .onNodeWithText("Any additional info?")
            .performClick()

        composeTestRule
            .onNodeWithText("Any additional info?")
            .performTextInput("Had a great day!")

        // Verify callback received the text
        assertEquals("Had a great day!", enteredContent)
    }
}
