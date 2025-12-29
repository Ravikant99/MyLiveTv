package com.ravi.mylivetv.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.ravi.mylivetv.data.local.entity.ChannelEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface ChannelDao {
    
    @Query("SELECT * FROM channels WHERE categoryUrl = :categoryUrl ORDER BY name ASC")
    suspend fun getChannelsByCategory(categoryUrl: String): List<ChannelEntity>
    
    @Query("SELECT * FROM channels WHERE categoryUrl = :categoryUrl ORDER BY name ASC")
    fun getChannelsByCategoryFlow(categoryUrl: String): Flow<List<ChannelEntity>>
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertChannels(channels: List<ChannelEntity>)
    
    @Query("DELETE FROM channels WHERE categoryUrl = :categoryUrl")
    suspend fun deleteChannelsByCategory(categoryUrl: String)
    
    @Query("DELETE FROM channels")
    suspend fun deleteAllChannels()
    
    @Query("SELECT COUNT(*) FROM channels WHERE categoryUrl = :categoryUrl")
    suspend fun getChannelCount(categoryUrl: String): Int
    
    @Query("SELECT timestamp FROM channels WHERE categoryUrl = :categoryUrl LIMIT 1")
    suspend fun getCacheTimestamp(categoryUrl: String): Long?
    
    @Query("DELETE FROM channels WHERE timestamp < :expirationTime")
    suspend fun deleteExpiredChannels(expirationTime: Long)
}



