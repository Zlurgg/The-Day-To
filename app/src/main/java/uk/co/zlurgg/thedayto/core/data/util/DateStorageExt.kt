package uk.co.zlurgg.thedayto.core.data.util

import java.time.Instant
import java.time.LocalDate
import java.time.ZoneOffset

/**
 * Pure extension functions for date storage conversion.
 *
 * These are stateless mathematical conversions - no timezone dependency,
 * deterministic output. Tests don't need to fake these.
 *
 * Storage format: UTC midnight epoch seconds.
 * This provides a stable identifier that doesn't shift with timezone changes.
 */

/**
 * Converts LocalDate to storage epoch (UTC midnight).
 *
 * This is the canonical storage format for journal entry dates.
 *
 * @return Epoch seconds representing midnight UTC on this date
 */
fun LocalDate.toStorageEpoch(): Long =
    atStartOfDay().toEpochSecond(ZoneOffset.UTC)

/**
 * Converts storage epoch back to LocalDate.
 *
 * Assumes epoch represents UTC midnight (matches [toStorageEpoch] format).
 *
 * @return The LocalDate this epoch represents
 */
fun Long.toLocalDate(): LocalDate =
    Instant.ofEpochSecond(this)
        .atOffset(ZoneOffset.UTC)
        .toLocalDate()
