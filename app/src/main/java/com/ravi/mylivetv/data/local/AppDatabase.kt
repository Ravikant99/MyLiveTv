package com.ravi.mylivetv.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import com.ravi.mylivetv.data.local.dao.ChannelDao
import com.ravi.mylivetv.data.local.dao.RecentlyWatchedDao
import com.ravi.mylivetv.data.local.entity.ChannelEntity
import com.ravi.mylivetv.data.local.entity.RecentlyWatchedEntity

@Database(
    entities = [ChannelEntity::class, RecentlyWatchedEntity::class],
    version = 2, // Incremented version for new entity
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun channelDao(): ChannelDao
    abstract fun recentlyWatchedDao(): RecentlyWatchedDao
    
    companion object {
        const val DATABASE_NAME = "mylivetv_database"
    }
}

