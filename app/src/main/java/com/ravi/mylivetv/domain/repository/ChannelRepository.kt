package com.ravi.mylivetv.domain.repository

import com.ravi.mylivetv.domain.model.Channel
import com.ravi.mylivetv.utils.Resource

interface ChannelRepository {

    suspend fun getChannels(url: String, forceRefresh: Boolean = false): Resource<List<Channel>>
    
    fun clearCache()
}
