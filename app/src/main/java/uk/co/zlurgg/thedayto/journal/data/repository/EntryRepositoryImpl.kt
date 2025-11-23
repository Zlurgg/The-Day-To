package uk.co.zlurgg.thedayto.journal.data.repository

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import uk.co.zlurgg.thedayto.journal.data.dao.EntryDao
import uk.co.zlurgg.thedayto.journal.data.mapper.toDomain
import uk.co.zlurgg.thedayto.journal.data.mapper.toEntity
import uk.co.zlurgg.thedayto.journal.domain.model.Entry
import uk.co.zlurgg.thedayto.journal.domain.model.EntryWithMoodColor
import uk.co.zlurgg.thedayto.journal.domain.repository.EntryRepository
import java.time.LocalDate
import java.time.ZoneOffset

class EntryRepositoryImpl(
    private val dao: EntryDao
) : EntryRepository {
    override fun getEntries(): Flow<List<Entry>> {
        return dao.getEntries().map { entities ->
            entities.map { it.toDomain() }
        }
    }

    override fun getEntriesWithMoodColors(): Flow<List<EntryWithMoodColor>> {
        return dao.getEntriesWithMoodColors().map { entities ->
            entities.map { it.toDomain() }
        }
    }

    override fun getEntriesForMonth(month: Int, year: Int): Flow<List<EntryWithMoodColor>> {
        require(month in 1..12) { "Month must be between 1 and 12, got: $month" }
        require(year > 0) { "Year must be positive, got: $year" }

        val (startEpoch, endEpoch) = getMonthRange(month, year)

        return dao.getEntriesForMonth(startEpoch, endEpoch).map { entities ->
            entities.map { it.toDomain() }
        }
    }

    /**
     * Calculate the epoch second range for a given month.
     *
     * @param month Month value (1-12)
     * @param year Year value
     * @return Pair of (startEpoch, endEpoch) where start is inclusive and end is exclusive
     */
    private fun getMonthRange(month: Int, year: Int): Pair<Long, Long> {
        val startOfMonth = LocalDate.of(year, month, 1)
            .atStartOfDay()
            .toEpochSecond(ZoneOffset.UTC)

        val startOfNextMonth = LocalDate.of(year, month, 1)
            .plusMonths(1)
            .atStartOfDay()
            .toEpochSecond(ZoneOffset.UTC)

        return Pair(startOfMonth, startOfNextMonth)
    }

    override suspend fun getEntryById(id: Int): Entry? {
        return dao.getEntryById(id)?.toDomain()
    }

    override suspend fun getEntryWithMoodColorById(id: Int): EntryWithMoodColor? {
        return dao.getEntryWithMoodColorById(id)?.toDomain()
    }

    override suspend fun getEntryByDate(date: Long): Entry? {
        return dao.getEntryByDate(date)?.toDomain()
    }

    override suspend fun getEntryWithMoodColorByDate(date: Long): EntryWithMoodColor? {
        return dao.getEntryWithMoodColorByDate(date)?.toDomain()
    }

    override suspend fun insertEntry(entry: Entry) {
        return dao.insertEntry(entry.toEntity())
    }

    override suspend fun deleteEntry(entry: Entry) {
        return dao.deleteEntry(entry.toEntity())
    }

    override suspend fun updateEntry(entry: Entry) {
        return dao.updateEntry(entry.toEntity())
    }
}