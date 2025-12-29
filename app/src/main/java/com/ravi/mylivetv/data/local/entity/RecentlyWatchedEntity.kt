package com.ravi.mylivetv.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "recently_watched")
data class RecentlyWatchedEntity(
    @PrimaryKey
    val streamUrl: String, // Use streamUrl as primary key to avoid duplicates
    val channelName: String,
    val channelLogo: String,
    val category: String,
    val categoryUrl: String, // To filter by category/language/country
    val lastWatchedTime: Long = System.currentTimeMillis()
)



