package com.ravi.mylivetv.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.ravi.mylivetv.data.local.entity.RecentlyWatchedEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface RecentlyWatchedDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdate(recentlyWatched: RecentlyWatchedEntity)
    
    @Update
    suspend fun update(recentlyWatched: RecentlyWatchedEntity)
    
    @Query("SELECT * FROM recently_watched WHERE streamUrl = :streamUrl LIMIT 1")
    suspend fun getByStreamUrl(streamUrl: String): RecentlyWatchedEntity?

    @Query("SELECT * FROM recently_watched WHERE categoryUrl = :categoryUrl ORDER BY lastWatchedTime DESC LIMIT :limit")
    suspend fun getRecentlyWatchedByCategory(categoryUrl: String, limit: Int = 10): List<RecentlyWatchedEntity>

    @Query("SELECT * FROM recently_watched ORDER BY lastWatchedTime DESC")
    fun getAllRecentlyWatchedFlow(): Flow<List<RecentlyWatchedEntity>>

    @Query("SELECT * FROM recently_watched ORDER BY lastWatchedTime DESC LIMIT :limit")
    suspend fun getAllRecentlyWatched(limit: Int = 50): List<RecentlyWatchedEntity>

    @Query("DELETE FROM recently_watched WHERE streamUrl = :streamUrl")
    suspend fun deleteByStreamUrl(streamUrl: String)

    @Query("DELETE FROM recently_watched")
    suspend fun clearAll()

    @Query("DELETE FROM recently_watched WHERE lastWatchedTime < :expirationTime")
    suspend fun deleteOldEntries(expirationTime: Long)
}

