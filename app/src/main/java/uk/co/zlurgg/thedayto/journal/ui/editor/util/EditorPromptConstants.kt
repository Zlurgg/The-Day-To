package uk.co.zlurgg.thedayto.journal.ui.editor.util

/**
 * Editor prompt constants for the mood entry screen.
 *
 * Provides multiple prompt variations based on the selected date (today, past, or future)
 * to create a more natural and contextual user experience.
 */
object EditorPromptConstants {

    /**
     * Prompts for today's entry
     */
    val TODAY_PROMPTS = listOf(
        "How're you feeling today?",
        "How are you doing today?",
        "What's your mood today?",
        "How do you feel right now?",
        "What's your vibe today?",
        "How's your day going?"
    )

    /**
     * Prompts for past dates
     */
    val PAST_PROMPTS = listOf(
        "How were you feeling that day?",
        "How did you feel?",
        "What was your mood?",
        "How were you doing?",
        "What was your vibe that day?",
        "How did that day go?"
    )

    /**
     * Prompts for future dates (if needed)
     */
    val FUTURE_PROMPTS = listOf(
        "How do you think you'll feel?",
        "What mood are you anticipating?",
        "How might you feel?",
        "What's your expected mood?",
        "How do you expect to feel?",
        "Planning your future mood?"
    )
}
