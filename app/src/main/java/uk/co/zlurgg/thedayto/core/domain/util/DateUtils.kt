package uk.co.zlurgg.thedayto.core.domain.util

import java.time.LocalDate
import java.time.ZoneOffset

/**
 * Utility functions for date operations.
 *
 * @deprecated Use [TimeProvider] for time access and [toStorageEpoch] extension for conversion.
 * This object will be removed after migration is complete.
 */
@Deprecated("Use TimeProvider for time access")
object DateUtils {
    /**
     * Gets the epoch seconds for the start of today (00:00:00 UTC).
     *
     * @return Today's date at midnight as epoch seconds
     * @deprecated Use [TimeProvider.todayStorageEpoch] instead for testable time access.
     */
    @Deprecated(
        message = "Use TimeProvider.todayStorageEpoch() for testable time access",
        replaceWith = ReplaceWith(
            expression = "timeProvider.todayStorageEpoch()",
            imports = ["uk.co.zlurgg.thedayto.core.domain.util.TimeProvider"]
        )
    )
    fun getTodayStartEpoch(): Long {
        return LocalDate.now().atStartOfDay()
            .toEpochSecond(ZoneOffset.UTC)
    }
}
