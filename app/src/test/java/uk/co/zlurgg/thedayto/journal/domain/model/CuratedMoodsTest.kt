package uk.co.zlurgg.thedayto.journal.domain.model

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * Validates the curated mood pool's integrity.
 * Guards against accidental duplicates when adding new moods.
 */
class CuratedMoodsTest {

    @Test
    fun `all hex codes are unique`() {
        val hexCodes = CuratedMoods.ALL.map { it.color }
        assertEquals(
            "Duplicate hex codes found: ${hexCodes.groupBy { it }.filter { it.value.size > 1 }.keys}",
            hexCodes.size,
            hexCodes.toSet().size,
        )
    }

    @Test
    fun `all mood names are unique (case-insensitive)`() {
        val names = CuratedMoods.ALL.map { it.mood.lowercase() }
        assertEquals(
            "Duplicate mood names found: ${names.groupBy { it }.filter { it.value.size > 1 }.keys}",
            names.size,
            names.toSet().size,
        )
    }

    @Test
    fun `no overlap with default seed moods`() {
        // The 7 default seeds in SeedDefaultMoodColorsUseCase
        val defaults = setOf("happy", "sad", "in love", "calm", "excited", "anxious", "grateful")
        val curatedNames = CuratedMoods.ALL.map { it.mood.lowercase() }.toSet()
        val overlap = defaults.intersect(curatedNames)
        assertTrue("Curated pool overlaps with defaults: $overlap", overlap.isEmpty())
    }

    @Test
    fun `all hex codes are valid 6-character hex`() {
        val hexRegex = Regex("^[A-Fa-f0-9]{6}$")
        CuratedMoods.ALL.forEach { seed ->
            assertTrue(
                "'${seed.mood}' has invalid hex: '${seed.color}'",
                seed.color.matches(hexRegex),
            )
        }
    }

    @Test
    fun `no blank mood names`() {
        CuratedMoods.ALL.forEach { seed ->
            assertTrue(
                "Blank mood name found at color ${seed.color}",
                seed.mood.isNotBlank(),
            )
        }
    }

    @Test
    fun `pool has at least 50 moods for good dice coverage`() {
        assertTrue(
            "Pool has ${CuratedMoods.ALL.size} moods, expected at least 50",
            CuratedMoods.ALL.size >= 50,
        )
    }
}
