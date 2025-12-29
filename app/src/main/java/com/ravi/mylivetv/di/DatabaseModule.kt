package com.ravi.mylivetv.di

import android.content.Context
import androidx.room.Room
import com.ravi.mylivetv.data.local.AppDatabase
import com.ravi.mylivetv.data.local.dao.ChannelDao
import com.ravi.mylivetv.data.local.dao.RecentlyWatchedDao
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Provides
    @Singleton
    fun provideAppDatabase(
        @ApplicationContext context: Context
    ): AppDatabase {
        return Room.databaseBuilder(
            context,
            AppDatabase::class.java,
            AppDatabase.DATABASE_NAME
        )
            .fallbackToDestructiveMigration()
            .build()
    }

    @Provides
    @Singleton
    fun provideChannelDao(database: AppDatabase): ChannelDao {
        return database.channelDao()
    }

    @Provides
    @Singleton
    fun provideRecentlyWatchedDao(database: AppDatabase): RecentlyWatchedDao {
        return database.recentlyWatchedDao()
    }
}

