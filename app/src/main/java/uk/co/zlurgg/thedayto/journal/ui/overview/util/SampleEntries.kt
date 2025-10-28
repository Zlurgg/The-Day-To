package uk.co.zlurgg.thedayto.journal.ui.overview.util

import uk.co.zlurgg.thedayto.journal.domain.model.Entry
import java.time.LocalDate
import java.time.ZoneOffset

/**
 * Sample entries for previews and testing
 * Uses current month's 15th, 20th, and 25th to ensure visibility regardless of current date
 */
object SampleEntries {
    private val currentMonth = LocalDate.now().withDayOfMonth(1)

    val sampleEntry1 = Entry(
        mood = "Happy",
        content = "Had a great day at work! Finished the new feature and got positive feedback from the team.",
        dateStamp = currentMonth.withDayOfMonth(15).atStartOfDay().toEpochSecond(ZoneOffset.UTC),
        color = "4CAF50",
        id = 1
    )

    val sampleEntry2 = Entry(
        mood = "Peaceful",
        content = "Spent time reading in the park. The weather was perfect.",
        dateStamp = currentMonth.withDayOfMonth(20).atStartOfDay().toEpochSecond(ZoneOffset.UTC),
        color = "2196F3",
        id = 2
    )

    val sampleEntry3 = Entry(
        mood = "Motivated",
        content = "Started learning Kotlin Compose. Excited about building modern Android apps!",
        dateStamp = currentMonth.withDayOfMonth(25).atStartOfDay().toEpochSecond(ZoneOffset.UTC),
        color = "FF9800",
        id = 3
    )

    val allSamples = listOf(sampleEntry1, sampleEntry2, sampleEntry3)
}
