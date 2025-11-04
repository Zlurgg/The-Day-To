package uk.co.zlurgg.thedayto.core.domain.util

import java.time.LocalDate
import java.time.ZoneOffset


/** TODO: this contains java logic check that we can use this here or move it somewhere else ui
 * Utility functions for date operations.
 * Centralizes date-related business logic.
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
