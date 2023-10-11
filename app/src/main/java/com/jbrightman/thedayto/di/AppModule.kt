package com.jbrightman.thedayto.di

import android.app.Application
import androidx.room.Room
import com.jbrightman.thedayto.data.data_source.TheDayToDatabase
import com.jbrightman.thedayto.feature_mood_color.data.repository.MoodColorRepositoryImpl
import com.jbrightman.thedayto.feature_daily_entry.data.repository.DailyEntryRepositoryImpl
import com.jbrightman.thedayto.feature_mood_color.domain.repository.MoodColorRepository
import com.jbrightman.thedayto.feature_daily_entry.domain.repository.DailyEntryRepository
import com.jbrightman.thedayto.feature_daily_entry.domain.use_case.AddDailyEntry
import com.jbrightman.thedayto.feature_daily_entry.domain.use_case.DeleteDailyEntryUseCase
import com.jbrightman.thedayto.feature_daily_entry.domain.use_case.DailyEntryUseCases
import com.jbrightman.thedayto.feature_daily_entry.domain.use_case.GetDailyEntriesUseCase
import com.jbrightman.thedayto.feature_daily_entry.domain.use_case.GetDailyEntry
import com.jbrightman.thedayto.feature_daily_entry.domain.use_case.UpdateDailyEntry
import com.jbrightman.thedayto.feature_mood_color.domain.use_case.AddMoodColor
import com.jbrightman.thedayto.feature_mood_color.domain.use_case.DeleteMoodColorUseCase
import com.jbrightman.thedayto.feature_mood_color.domain.use_case.GetMoodColor
import com.jbrightman.thedayto.feature_mood_color.domain.use_case.GetMoodColorsUseCase
import com.jbrightman.thedayto.feature_mood_color.domain.use_case.MoodColorUseCases
import com.jbrightman.thedayto.feature_mood_color.domain.use_case.UpdateMoodColor
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    @Provides
    @Singleton
    fun provideTheDayToDatabase(app: Application): TheDayToDatabase {
        return Room.databaseBuilder(
            app,
            TheDayToDatabase::class.java,
            TheDayToDatabase.DATABASE_NAME
        ).build()
    }

    @Provides
    @Singleton
    fun providesTheDayToRepository(db: TheDayToDatabase): DailyEntryRepository {
        return DailyEntryRepositoryImpl(db.dailyEntryDao)
    }

    @Provides
    @Singleton
    fun provideTheDayToEntryUseCases(dailyEntryRepository: DailyEntryRepository): DailyEntryUseCases {
        return DailyEntryUseCases(
            getEntries = GetDailyEntriesUseCase(repository = dailyEntryRepository),
            deleteEntry = DeleteDailyEntryUseCase(repository = dailyEntryRepository),
            addDailyEntry = AddDailyEntry(repository = dailyEntryRepository),
            getDailyEntry = GetDailyEntry(repository = dailyEntryRepository),
            updateDailyEntry = UpdateDailyEntry(repository = dailyEntryRepository)
        )
    }

    @Provides
    @Singleton
    fun providesMoodColorRepository(db: TheDayToDatabase): MoodColorRepository {
        return MoodColorRepositoryImpl(db.moodColorDao)
    }

    @Provides
    @Singleton
    fun providesMoodColorUseCases(moodColorRepository: MoodColorRepository): MoodColorUseCases {
        return MoodColorUseCases(
            getMoodColors = GetMoodColorsUseCase(repository = moodColorRepository),
            deleteMoodColor = DeleteMoodColorUseCase(repository = moodColorRepository),
            addMoodColor = AddMoodColor(repository = moodColorRepository),
            getMoodColor = GetMoodColor(repository = moodColorRepository),
            updateMoodColor = UpdateMoodColor(repository = moodColorRepository)
        )
    }
}