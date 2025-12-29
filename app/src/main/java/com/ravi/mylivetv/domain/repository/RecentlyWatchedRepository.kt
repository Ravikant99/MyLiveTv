package com.ravi.mylivetv.domain.repository

import com.ravi.mylivetv.data.local.entity.RecentlyWatchedEntity

interface RecentlyWatchedRepository {
    suspend fun saveRecentlyWatched(
        streamUrl: String,
        channelName: String,
        channelLogo: String,
        category: String,
        categoryUrl: String
    )

    suspend fun getRecentlyWatchedByCategory(categoryUrl: String, limit: Int = 10): List<RecentlyWatchedEntity>

    suspend fun getAllRecentlyWatched(limit: Int = 50): List<RecentlyWatchedEntity>

    suspend fun clearAll()
}



