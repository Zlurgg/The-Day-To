package uk.co.zlurgg.thedayto.journal.domain.model

/**
 * Curated pool of mood+color pairs for the random seeding feature.
 *
 * These are independent of the 7 default seeds in [SeedDefaultMoodColorsUseCase]
 * (Happy, Sad, In Love, Calm, Excited, Anxious, Grateful) which have special
 * sync behaviour (fixed syncId, updatedAt = 0). Curated moods are created as
 * normal user moods via [SaveMoodColorUseCase].
 *
 * Design principles:
 * - Colors grounded in color psychology (warm reds for intensity, cool blues for calm, etc.)
 * - Every hex code is unique so moods are distinguishable on the calendar
 * - Short names (1-2 words) for comfortable dropdown display
 * - Broad emotional range: positive, negative, neutral, social, reflective, energetic
 */
object CuratedMoods {

    data class Seed(val mood: String, val color: String)

    val ALL: List<Seed> = listOf(
        // Positive / Warm
        Seed("Joyful", "FFD54F"),
        Seed("Content", "A5D6A7"),
        Seed("Hopeful", "FFF176"),
        Seed("Playful", "FF8A65"),
        Seed("Proud", "CE93D8"),
        Seed("Cheerful", "FFB74D"),
        Seed("Amused", "FFD740"),
        Seed("Inspired", "7E57C2"),
        Seed("Optimistic", "81D4FA"),
        Seed("Relieved", "80CBC4"),

        // Energetic / Intense
        Seed("Passionate", "EF5350"),
        Seed("Determined", "FF7043"),
        Seed("Confident", "42A5F5"),
        Seed("Energised", "66BB6A"),
        Seed("Adventurous", "FFAB40"),
        Seed("Empowered", "AB47BC"),
        Seed("Fired Up", "F44336"),
        Seed("Motivated", "4CAF50"),
        Seed("Fearless", "FF5722"),

        // Reflective / Neutral
        Seed("Thoughtful", "90A4AE"),
        Seed("Curious", "4DD0E1"),
        Seed("Nostalgic", "D4A574"),
        Seed("Mellow", "C5E1A5"),
        Seed("Pensive", "78909C"),
        Seed("Contemplative", "B0BEC5"),
        Seed("Daydreamy", "B39DDB"),
        Seed("Peaceful", "81C784"),
        Seed("Wistful", "BCAAA4"),
        Seed("Serene", "4FC3F7"),

        // Mixed / Unsettled
        Seed("Restless", "E07C4F"),
        Seed("Conflicted", "FFA270"),
        Seed("Numb", "CFD8DC"),
        Seed("Uncertain", "AED581"),

        // Social / Connected
        Seed("Loved", "F48FB1"),
        Seed("Affectionate", "F06292"),
        Seed("Empathetic", "9FA8DA"),
        Seed("Thankful", "009688"),
        Seed("Connected", "26C6DA"),
        Seed("Compassionate", "BA68C8"),

        // Low Energy / Quiet
        Seed("Tired", "9E9E9E"),
        Seed("Lazy", "BDBDBD"),
        Seed("Bored", "B8C4CA"),
        Seed("Drained", "6B7B8D"),
        Seed("Sleepy", "7986CB"),
        Seed("Lethargic", "A1887F"),

        // Negative / Difficult
        Seed("Frustrated", "E57373"),
        Seed("Overwhelmed", "7B1FA2"),
        Seed("Irritated", "D4845A"),
        Seed("Lonely", "5C6BC0"),
        Seed("Jealous", "689F38"),
        Seed("Embarrassed", "E91E63"),
        Seed("Guilty", "8D6E63"),
        Seed("Vulnerable", "D1A3E0"),
        Seed("Melancholy", "3F51B5"),
        Seed("Worried", "9575CD"),
        Seed("Insecure", "C9A882"),
        Seed("Angry", "D32F2F"),
        Seed("Scared", "512DA8"),
        Seed("Disappointed", "7E8C8D"),
        Seed("Heartbroken", "880E4F"),
        Seed("Stressed", "E040FB"),
    )
}
