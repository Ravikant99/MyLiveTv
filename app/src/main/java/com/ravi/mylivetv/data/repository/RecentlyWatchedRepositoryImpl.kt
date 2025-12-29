package com.ravi.mylivetv.data.repository

import android.util.Log
import com.ravi.mylivetv.data.local.dao.RecentlyWatchedDao
import com.ravi.mylivetv.data.local.entity.RecentlyWatchedEntity
import com.ravi.mylivetv.domain.repository.RecentlyWatchedRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton

private const val TAG = "RecentlyWatchedRepo"

@Singleton
class RecentlyWatchedRepositoryImpl @Inject constructor(
    private val recentlyWatchedDao: RecentlyWatchedDao
) : RecentlyWatchedRepository {

    override suspend fun saveRecentlyWatched(
        streamUrl: String,
        channelName: String,
        channelLogo: String,
        category: String,
        categoryUrl: String
    ) {
        withContext(Dispatchers.IO) {
            val currentTime = System.currentTimeMillis()
            
            // Check if entry already exists
            val existing = recentlyWatchedDao.getByStreamUrl(streamUrl)
            
            if (existing != null) {
                Log.d(TAG, "📺 Updating existing entry: $channelName (old: ${existing.lastWatchedTime}, new: $currentTime)")
                // Delete old entry first to ensure fresh insert
                recentlyWatchedDao.deleteByStreamUrl(streamUrl)
            } else {
                Log.d(TAG, "📺 Creating new entry: $channelName (timestamp: $currentTime)")
            }
            
            val entity = RecentlyWatchedEntity(
                streamUrl = streamUrl,
                channelName = channelName,
                channelLogo = channelLogo,
                category = category,
                categoryUrl = categoryUrl,
                lastWatchedTime = currentTime
            )
            recentlyWatchedDao.insertOrUpdate(entity)
            
            // Verify the save
            val saved = recentlyWatchedDao.getByStreamUrl(streamUrl)
            Log.d(TAG, "✅ Verified save: ${saved?.channelName} (timestamp: ${saved?.lastWatchedTime})")
        }
    }

    override suspend fun getRecentlyWatchedByCategory(categoryUrl: String, limit: Int): List<RecentlyWatchedEntity> = 
        withContext(Dispatchers.IO) {
            recentlyWatchedDao.getRecentlyWatchedByCategory(categoryUrl, limit)
        }

    override suspend fun getAllRecentlyWatched(limit: Int): List<RecentlyWatchedEntity> = 
        withContext(Dispatchers.IO) {
            val list = recentlyWatchedDao.getAllRecentlyWatched(limit)
            Log.d(TAG, "📋 Retrieved ${list.size} recently watched channels")
            list.forEachIndexed { index, entity ->
                Log.d(TAG, "  ${index + 1}. ${entity.channelName} (time: ${entity.lastWatchedTime})")
            }
            list
        }

    override suspend fun clearAll() {
        withContext(Dispatchers.IO) {
            recentlyWatchedDao.clearAll()
        }
    }
}

