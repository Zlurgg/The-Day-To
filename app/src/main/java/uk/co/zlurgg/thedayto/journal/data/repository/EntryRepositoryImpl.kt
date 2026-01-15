package uk.co.zlurgg.thedayto.journal.data.repository

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import io.github.zlurgg.core.domain.error.DataError
import io.github.zlurgg.core.domain.error.ErrorMapper
import io.github.zlurgg.core.domain.result.EmptyResult
import io.github.zlurgg.core.domain.result.Result
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

    override suspend fun getEntryById(id: Int): Result<Entry?, DataError.Local> {
        return ErrorMapper.safeSuspendCall(TAG) {
            dao.getEntryById(id)?.toDomain()
        }
    }

    override suspend fun getEntryWithMoodColorById(id: Int): Result<EntryWithMoodColor?, DataError.Local> {
        return ErrorMapper.safeSuspendCall(TAG) {
            dao.getEntryWithMoodColorById(id)?.toDomain()
        }
    }

    override suspend fun getEntryByDate(date: Long): Result<Entry?, DataError.Local> {
        return ErrorMapper.safeSuspendCall(TAG) {
            dao.getEntryByDate(date)?.toDomain()
        }
    }

    override suspend fun getEntryWithMoodColorByDate(date: Long): Result<EntryWithMoodColor?, DataError.Local> {
        return ErrorMapper.safeSuspendCall(TAG) {
            dao.getEntryWithMoodColorByDate(date)?.toDomain()
        }
    }

    override suspend fun insertEntry(entry: Entry): EmptyResult<DataError.Local> {
        return ErrorMapper.safeSuspendCall(TAG) {
            dao.insertEntry(entry.toEntity())
        }
    }

    override suspend fun deleteEntry(entry: Entry): EmptyResult<DataError.Local> {
        return ErrorMapper.safeSuspendCall(TAG) {
            dao.deleteEntry(entry.toEntity())
        }
    }

    override suspend fun updateEntry(entry: Entry): EmptyResult<DataError.Local> {
        return ErrorMapper.safeSuspendCall(TAG) {
            dao.updateEntry(entry.toEntity())
        }
    }

    companion object {
        private const val TAG = "EntryRepository"
    }

    override fun getMoodColorEntryCounts(): Flow<Map<Int, Int>> {
        return dao.getMoodColorEntryCounts().map { counts ->
            counts.associate { it.moodColorId to it.entryCount }
        }
    }
}