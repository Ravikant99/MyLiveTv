package com.ravi.mylivetv.data.repository

import android.util.Log
import com.ravi.mylivetv.data.local.dao.ChannelDao
import com.ravi.mylivetv.data.local.entity.toEntity
import com.ravi.mylivetv.data.local.entity.toDomain
import com.ravi.mylivetv.data.model.toDomain
import com.ravi.mylivetv.data.parser.M3UParser
import com.ravi.mylivetv.data.remote.PlaylistService
import com.ravi.mylivetv.domain.model.Channel
import com.ravi.mylivetv.domain.repository.ChannelRepository
import com.ravi.mylivetv.utils.Resource
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ChannelRepositoryImpl @Inject constructor(
    private val service: PlaylistService,
    private val parser: M3UParser,
    private val channelDao: ChannelDao
) : ChannelRepository {

    private val TAG = "ChannelRepository"
    
    // Cache expiration time: 24 hours
    private val CACHE_EXPIRATION_MS = 24 * 60 * 60 * 1000L

    override suspend fun getChannels(url: String, forceRefresh: Boolean): Resource<List<Channel>> {
        return withContext(Dispatchers.IO) {
            try {
                // 1. Check Room DB cache
                if (!forceRefresh) {
                    val cachedChannels = channelDao.getChannelsByCategory(url)
                    if (cachedChannels.isNotEmpty()) {
                        val cacheTimestamp = channelDao.getCacheTimestamp(url) ?: 0L
                        val age = System.currentTimeMillis() - cacheTimestamp
                        
                        if (age < CACHE_EXPIRATION_MS) {
                            val channels = cachedChannels.map { it.toDomain() }
                            Log.d(TAG, "✅ DB Cache HIT for: $url (${channels.size} channels, age: ${age / 1000 / 60}min)")
                            return@withContext Resource.Success(channels)
                        } else {
                            Log.d(TAG, "⏰ DB Cache EXPIRED for: $url (age: ${age / 1000 / 60 / 60}hrs)")
                            channelDao.deleteChannelsByCategory(url)
                        }
                    }
                }
                
                // 2. Fetch from network
                Log.d(TAG, "🌐 Fetching from network: $url")
                val raw = service.fetchPlaylist(url)
                val dtos = parser.parse(raw)
                val channels = dtos.map { it.toDomain() }
                
                // 3. Save to Room DB
                val entities = channels.map { it.toEntity(url) }
                channelDao.deleteChannelsByCategory(url)
                channelDao.insertChannels(entities)
                Log.d(TAG, "💾 Saved to DB: ${channels.size} channels for: $url")
                
                // 4. Clean up expired caches
                cleanupExpiredCaches()
                
                Resource.Success(channels)
            } catch (e: Exception) {
                Log.e(TAG, "❌ Failed to load channels from: $url", e)
                
                // Try to return stale cache on error
                val cachedChannels = channelDao.getChannelsByCategory(url)
                if (cachedChannels.isNotEmpty()) {
                    val channels = cachedChannels.map { it.toDomain() }
                    Log.d(TAG, "⚠️ Returning stale cache on error: ${channels.size} channels")
                    Resource.Success(channels)
                } else {
                    Resource.Error("Failed to load channels: ${e.message}", e)
                }
            }
        }
    }
    
    override fun clearCache() {
        Log.d(TAG, "🗑️ Clearing DB cache (async)")
        // Note: DB cache is cleared asynchronously when needed
    }
    
    private suspend fun cleanupExpiredCaches() {
        try {
            val expirationTime = System.currentTimeMillis() - CACHE_EXPIRATION_MS
            channelDao.deleteExpiredChannels(expirationTime)
            Log.d(TAG, "🧹 Cleaned up expired caches")
        } catch (e: Exception) {
            Log.e(TAG, "Failed to cleanup expired caches", e)
        }
    }
}

