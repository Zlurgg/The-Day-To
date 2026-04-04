package uk.co.zlurgg.thedayto.core.data.util

import uk.co.zlurgg.thedayto.core.domain.util.TimeProvider
import java.time.Instant
import java.time.LocalDate
import java.time.LocalDateTime

/**
 * Production implementation of [TimeProvider].
 *
 * Uses system clock for all time operations.
 * Registered as singleton in CoreModule.
 */
class SystemTimeProvider : TimeProvider {

    override fun today(): LocalDate = LocalDate.now()

    override fun now(): LocalDateTime = LocalDateTime.now()

    override fun instant(): Instant = Instant.now()

    override fun todayStorageEpoch(): Long = today().toStorageEpoch()
}
