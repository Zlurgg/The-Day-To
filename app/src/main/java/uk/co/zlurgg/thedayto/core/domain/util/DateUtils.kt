package uk.co.zlurgg.thedayto.core.domain.util

import java.time.LocalDate
import java.time.ZoneOffset

/**
 * Utility functions for date operations.
 * Centralizes date-related business logic.
 *
 * Note: Uses standard Java Time API (java.time.*) which is allowed in the domain layer.
 * Domain layer can use standard Java/Kotlin libraries - it's Android framework dependencies
 * (e.g., android.* packages) that should be avoided to maintain framework independence.
 */
object DateUtils {
    /**
     * Gets the epoch seconds for the start of today (00:00:00 UTC).
     *
     * @return Today's date at midnight as epoch seconds
     */
    fun getTodayStartEpoch(): Long {
        return LocalDate.now().atStartOfDay()
            .toEpochSecond(ZoneOffset.UTC)
    }
}
