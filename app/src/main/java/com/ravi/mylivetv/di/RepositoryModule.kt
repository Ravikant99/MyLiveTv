package com.ravi.mylivetv.di

import com.ravi.mylivetv.data.repository.ChannelRepositoryImpl
import com.ravi.mylivetv.data.repository.RecentlyWatchedRepositoryImpl
import com.ravi.mylivetv.domain.repository.ChannelRepository
import com.ravi.mylivetv.domain.repository.RecentlyWatchedRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class RepositoryModule {

    @Binds
    @Singleton
    abstract fun bindChannelRepository(
        channelRepositoryImpl: ChannelRepositoryImpl
    ): ChannelRepository

    @Binds
    @Singleton
    abstract fun bindRecentlyWatchedRepository(
        recentlyWatchedRepositoryImpl: RecentlyWatchedRepositoryImpl
    ): RecentlyWatchedRepository
}

