package com.jbrightman.thedayto.di

import android.app.Application
import androidx.room.Room
import com.jbrightman.thedayto.feature_thedayto.data.data_source.TheDayToDatabase
import com.jbrightman.thedayto.feature_thedayto.data.repository.mood_color.MoodColorRepositoryImpl
import com.jbrightman.thedayto.feature_thedayto.data.repository.entry.TheDayToRepositoryImpl
import com.jbrightman.thedayto.feature_thedayto.domain.repository.mood_color.MoodColorRepository
import com.jbrightman.thedayto.feature_thedayto.domain.repository.entry.TheDayToRepository
import com.jbrightman.thedayto.feature_thedayto.domain.use_case.entry.AddEntry
import com.jbrightman.thedayto.feature_thedayto.domain.use_case.entry.DeleteEntryUseCase
import com.jbrightman.thedayto.feature_thedayto.domain.use_case.entry.EntryUseCases
import com.jbrightman.thedayto.feature_thedayto.domain.use_case.entry.GetEntriesUseCase
import com.jbrightman.thedayto.feature_thedayto.domain.use_case.entry.GetEntry
import com.jbrightman.thedayto.feature_thedayto.domain.use_case.entry.UpdateEntry
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
    fun providesTheDayToRepository(db: TheDayToDatabase): TheDayToRepository {
        return TheDayToRepositoryImpl(db.theDayToDao)
    }

    @Provides
    @Singleton
    fun provideTheDayToEntryUseCases(theDayToRepository: TheDayToRepository): EntryUseCases {
        return EntryUseCases(
            getEntries = GetEntriesUseCase(repository = theDayToRepository),
            deleteEntry = DeleteEntryUseCase(repository = theDayToRepository),
            addEntry = AddEntry(repository = theDayToRepository),
            getEntry = GetEntry(repository = theDayToRepository),
            updateEntry = UpdateEntry(repository = theDayToRepository)
        )
    }

    @Provides
    @Singleton
    fun providesMoodColorRepository(db: TheDayToDatabase): MoodColorRepository {
        return MoodColorRepositoryImpl(db.moodColorDao)
    }
}