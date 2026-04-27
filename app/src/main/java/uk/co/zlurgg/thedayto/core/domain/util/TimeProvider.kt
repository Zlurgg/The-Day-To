package uk.co.zlurgg.thedayto.core.domain.util

import java.time.Instant
import java.time.LocalDate
import java.time.LocalDateTime

/**
 * Abstraction for time access, enabling testability.
 *
 * Provides current date/time and atomic convenience methods.
 * Implementations: [SystemTimeProvider] for production, [FakeTimeProvider] for tests.
 *
 * **java.time in domain:** We considered extracting this to the data layer with a
 * primitive-based mapper in domain (returning Longs/Ints) for strict layer purity.
 * We chose to keep java.time here because it is a JDK standard library type — not
 * an Android/framework dependency — and wrapping it would add friction to every
 * use case that does date logic without meaningful architectural benefit.
 *
 * Note: Storage conversion (epoch <-> LocalDate) is handled by pure extension
 * functions in DateStorageExt.kt, not this interface.
 */
interface TimeProvider {

    /**
     * Current local date (user's timezone).
     */
    fun today(): LocalDate

    /**
     * Current local date and time (user's timezone).
     */
    fun now(): LocalDateTime

    /**
     * Current instant (absolute point in time).
     */
    fun instant(): Instant

    /**
     * Atomic capture of today's date as storage epoch.
     *
     * Prevents midnight boundary races between calling [today] and converting
     * to storage format separately.
     *
     * @return Today's date as UTC midnight epoch seconds
     */
    fun todayStorageEpoch(): Long
}
