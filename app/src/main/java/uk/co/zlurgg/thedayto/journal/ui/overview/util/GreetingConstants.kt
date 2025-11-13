package uk.co.zlurgg.thedayto.journal.ui.overview.util

/**
 * Greeting message constants for the overview screen.
 *
 * Provides multiple greeting options for each time period to add variety
 * and warmth to the user experience.
 */
object GreetingConstants {

    /**
     * Morning greetings (5am - 11am)
     */
    val MORNING_GREETINGS = listOf(
        "Good morning",
        "Rise and shine",
        "Morning",
        "Hello sunshine",
        "Fresh start today",
        "New day, new possibilities"
    )

    /**
     * Afternoon greetings (12pm - 4pm)
     */
    val AFTERNOON_GREETINGS = listOf(
        "Good afternoon",
        "Hello there",
        "Afternoon",
        "Hope your day's going well",
        "Making progress today",
        "Halfway through the day"
    )

    /**
     * Evening greetings (5pm - 8pm)
     */
    val EVENING_GREETINGS = listOf(
        "Good evening",
        "Evening",
        "How was your day",
        "Winding down",
        "Almost done for today",
        "Hope you had a good day"
    )

    /**
     * Night greetings (9pm - 4am)
     */
    val NIGHT_GREETINGS = listOf(
        "Good night",
        "Evening",
        "Late night reflection",
        "Time to unwind",
        "End of the day",
        "Rest well soon"
    )
}
