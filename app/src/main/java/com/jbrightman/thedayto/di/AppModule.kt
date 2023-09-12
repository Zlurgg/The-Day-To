package com.jbrightman.thedayto.di

import android.app.Application
import androidx.room.Room
import com.jbrightman.thedayto.feature_thedayto.data.data_source.TheDayToDatabase
import com.jbrightman.thedayto.feature_thedayto.data.repository.TheDayToRepositoryImpl
import com.jbrightman.thedayto.feature_thedayto.domain.repository.TheDayToRepository
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

    //TODO Use Cases
/*    @Provides
    @Singleton
    fun provideTheDayToUseCases(theDayToRepository: TheDayToRepository): TheDayToUseCases {
        return TheDayToUseCases(
            addMood = GetTheDayToUseCases(repository = theDayToRepository),
        )
    }*/
}