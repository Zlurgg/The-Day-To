package uk.co.zlurgg.thedayto.di

import androidx.room.Room
import org.koin.android.ext.koin.androidApplication
import org.koin.dsl.module
import uk.co.zlurgg.thedayto.core.data.data_source.TheDayToDatabase
import uk.co.zlurgg.thedayto.feature_daily_entry.data.repository.DailyEntryRepositoryImpl
import uk.co.zlurgg.thedayto.feature_daily_entry.domain.repository.DailyEntryRepository
import uk.co.zlurgg.thedayto.feature_daily_entry.domain.use_case.AddDailyEntry
import uk.co.zlurgg.thedayto.feature_daily_entry.domain.use_case.DailyEntryUseCases
import uk.co.zlurgg.thedayto.feature_daily_entry.domain.use_case.DeleteDailyEntryUseCase
import uk.co.zlurgg.thedayto.feature_daily_entry.domain.use_case.GetDailyEntriesUseCase
import uk.co.zlurgg.thedayto.feature_daily_entry.domain.use_case.GetDailyEntry
import uk.co.zlurgg.thedayto.feature_daily_entry.domain.use_case.UpdateDailyEntry
import uk.co.zlurgg.thedayto.feature_mood_color.data.repository.MoodColorRepositoryImpl
import uk.co.zlurgg.thedayto.feature_mood_color.domain.repository.MoodColorRepository
import uk.co.zlurgg.thedayto.feature_mood_color.domain.use_case.AddMoodColor
import uk.co.zlurgg.thedayto.feature_mood_color.domain.use_case.DeleteMoodColorUseCase
import uk.co.zlurgg.thedayto.feature_mood_color.domain.use_case.GetMoodColor
import uk.co.zlurgg.thedayto.feature_mood_color.domain.use_case.GetMoodColorsUseCase
import uk.co.zlurgg.thedayto.feature_mood_color.domain.use_case.MoodColorUseCases
import uk.co.zlurgg.thedayto.feature_mood_color.domain.use_case.UpdateMoodColor

val appModule = module {

    single {
        Room.databaseBuilder(
            androidApplication(),
            TheDayToDatabase::class.java,
            TheDayToDatabase.DATABASE_NAME
        ).build()
    }

//    viewModelOf(::AddEditEntryViewModel)


    single<DailyEntryRepository> { DailyEntryRepositoryImpl(get<TheDayToDatabase>().dailyEntryDao) }

    single {
        DailyEntryUseCases(
            getEntries = GetDailyEntriesUseCase(repository = get()),
            deleteEntry = DeleteDailyEntryUseCase(repository = get()),
            addDailyEntry = AddDailyEntry(repository = get()),
            getDailyEntry = GetDailyEntry(repository = get()),
            updateDailyEntry = UpdateDailyEntry(repository = get())
        )
    }

    single<MoodColorRepository> { MoodColorRepositoryImpl(get<TheDayToDatabase>().moodColorDao) }

    single {
        MoodColorUseCases(
            getMoodColors = GetMoodColorsUseCase(repository = get()),
            deleteMoodColor = DeleteMoodColorUseCase(repository = get()),
            addMoodColor = AddMoodColor(repository = get()),
            getMoodColor = GetMoodColor(repository = get()),
            updateMoodColor = UpdateMoodColor(repository = get())
        )
    }

}