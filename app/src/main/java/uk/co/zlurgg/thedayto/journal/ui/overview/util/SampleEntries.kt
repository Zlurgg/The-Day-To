package uk.co.zlurgg.thedayto.journal.ui.overview.util

import uk.co.zlurgg.thedayto.journal.domain.model.Entry
import java.time.LocalDate
import java.time.ZoneOffset

/**
 * Sample entries for previews and testing
 * Uses current month's 15th, 20th, and 25th to ensure visibility regardless of current date
 *
 * Colors chosen to demonstrate auto-contrast text feature:
 * - Light yellow (FFF59D) - high luminance, triggers dark text
 * - Dark purple (4A148C) - low luminance, triggers light text
 * - Medium green (4CAF50) - medium luminance
 */
object SampleEntries {
    private val currentMonth = LocalDate.now().withDayOfMonth(1)

    val sampleEntry1 = Entry(
        mood = "Cheerful",
        content = "Had a great day at work! Finished the new feature and got positive feedback from the team.",
        dateStamp = currentMonth.withDayOfMonth(15).atStartOfDay().toEpochSecond(ZoneOffset.UTC),
        color = "FFF59D", // Light yellow - will use dark text
        id = 1
    )

    val sampleEntry2 = Entry(
        mood = "Calm",
        content = "Spent time reading in the park. The weather was perfect.",
        dateStamp = currentMonth.withDayOfMonth(20).atStartOfDay().toEpochSecond(ZoneOffset.UTC),
        color = "4A148C", // Dark purple - will use light text
        id = 2
    )

    val sampleEntry3 = Entry(
        mood = "Motivated",
        content = "Started learning Kotlin Compose. Excited about building modern Android apps!",
        dateStamp = currentMonth.withDayOfMonth(25).atStartOfDay().toEpochSecond(ZoneOffset.UTC),
        color = "4CAF50", // Medium green - will use dark text
        id = 3
    )

    val allSamples = listOf(sampleEntry1, sampleEntry2, sampleEntry3)
}
