package com.ravi.mylivetv.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.ravi.mylivetv.domain.model.Channel

@Entity(tableName = "channels")
data class ChannelEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val name: String,
    val logo: String,
    val streamUrl: String,
    val category: String,
    val categoryUrl: String, // The URL this channel belongs to (e.g., languages/hin.m3u)
    val timestamp: Long = System.currentTimeMillis() // For cache expiration
)

// Extension functions for mapping
fun ChannelEntity.toDomain(): Channel {
    return Channel(
        name = name,
        logo = logo,
        streamUrl = streamUrl,
        category = category
    )
}

fun Channel.toEntity(categoryUrl: String): ChannelEntity {
    return ChannelEntity(
        name = name,
        logo = logo,
        streamUrl = streamUrl,
        category = category,
        categoryUrl = categoryUrl
    )
}



