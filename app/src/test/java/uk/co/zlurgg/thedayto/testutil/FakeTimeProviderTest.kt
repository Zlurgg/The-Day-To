package uk.co.zlurgg.thedayto.testutil

import org.junit.Assert.assertEquals
import org.junit.Test
import uk.co.zlurgg.thedayto.core.data.util.toStorageEpoch
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.ZoneId

/**
 * Tests for FakeTimeProvider test utility.
 */
class FakeTimeProviderTest {

    @Test
    fun `today returns fixed date`() {
        val fixedDate = LocalDate.of(2024, 3, 20)
        val provider = FakeTimeProvider(fixedDate = fixedDate)

        assertEquals(fixedDate, provider.today())
    }

    @Test
    fun `now returns fixed date and time`() {
        val fixedDate = LocalDate.of(2024, 3, 20)
        val fixedTime = LocalTime.of(14, 30)
        val provider = FakeTimeProvider(fixedDate = fixedDate, fixedTime = fixedTime)

        assertEquals(LocalDateTime.of(fixedDate, fixedTime), provider.now())
    }

    @Test
    fun `todayStorageEpoch returns correct epoch for fixed date`() {
        val fixedDate = LocalDate.of(2024, 1, 1)
        val provider = FakeTimeProvider(fixedDate = fixedDate)

        assertEquals(fixedDate.toStorageEpoch(), provider.todayStorageEpoch())
    }

    @Test
    fun `instant respects fixedZone`() {
        val fixedDate = LocalDate.of(2024, 1, 1)
        val fixedTime = LocalTime.of(12, 0)

        // UTC: 12:00 -> instant at 12:00 UTC
        val utcProvider = FakeTimeProvider(
            fixedDate = fixedDate,
            fixedTime = fixedTime,
            fixedZone = ZoneId.of("UTC"),
        )

        // America/New_York (UTC-5): 12:00 -> instant at 17:00 UTC
        val nyProvider = FakeTimeProvider(
            fixedDate = fixedDate,
            fixedTime = fixedTime,
            fixedZone = ZoneId.of("America/New_York"),
        )

        // The NY instant should be 5 hours later than UTC instant
        val utcInstant = utcProvider.instant()
        val nyInstant = nyProvider.instant()

        // NY is UTC-5, so noon in NY is 17:00 UTC (5 hours later)
        assertEquals(
            utcInstant.plusSeconds(5 * 60 * 60),
            nyInstant,
        )
    }

    @Test
    fun `advanceDays moves date forward`() {
        val provider = FakeTimeProvider(fixedDate = LocalDate.of(2024, 1, 1))

        provider.advanceDays(10)

        assertEquals(LocalDate.of(2024, 1, 11), provider.today())
    }

    @Test
    fun `setDate changes fixed date`() {
        val provider = FakeTimeProvider(fixedDate = LocalDate.of(2024, 1, 1))

        provider.setDate(LocalDate.of(2024, 12, 25))

        assertEquals(LocalDate.of(2024, 12, 25), provider.today())
    }

    @Test
    fun `setTime changes fixed time`() {
        val provider = FakeTimeProvider(
            fixedDate = LocalDate.of(2024, 1, 1),
            fixedTime = LocalTime.of(9, 0),
        )

        provider.setTime(LocalTime.of(18, 30))

        assertEquals(
            LocalDateTime.of(2024, 1, 1, 18, 30),
            provider.now(),
        )
    }

    @Test
    fun `setZone changes timezone for instant`() {
        val provider = FakeTimeProvider(
            fixedDate = LocalDate.of(2024, 1, 1),
            fixedTime = LocalTime.of(12, 0),
            fixedZone = ZoneId.of("UTC"),
        )

        val utcInstant = provider.instant()

        provider.setZone(ZoneId.of("America/New_York"))
        val nyInstant = provider.instant()

        // After changing to NY timezone, same local time maps to different instant
        assertEquals(
            utcInstant.plusSeconds(5 * 60 * 60),
            nyInstant,
        )
    }
}
