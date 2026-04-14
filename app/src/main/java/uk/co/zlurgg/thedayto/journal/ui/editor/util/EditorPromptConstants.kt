package uk.co.zlurgg.thedayto.journal.ui.editor.util

/**
 * Editor prompt constants for the mood entry screen.
 *
 * Provides multiple prompt variations based on the selected date (today, past, or future)
 * to create a more natural and contextual user experience.
 */
object EditorPromptConstants {

    /** Alpha applied to hint text in the editor (mood and notes fields). */
    const val HINT_ALPHA = 0.8f

    /**
     * Prompts for today's entry
     */
    val TODAY_PROMPTS = listOf(
        "How're you feeling today?",
        "How are you doing today?",
        "What's your mood today?",
        "How do you feel right now?",
        "What's your vibe today?",
        "How's your day going?",
    )

    /**
     * Prompts for past dates
     */
    val PAST_PROMPTS = listOf(
        "How were you feeling?",
        "How did you feel?",
        "What was your mood?",
        "How were you doing?",
        "What was your vibe?",
        "How did that day go?",
    )
}
