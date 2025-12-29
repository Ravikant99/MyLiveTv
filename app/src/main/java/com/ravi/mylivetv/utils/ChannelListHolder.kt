package com.ravi.mylivetv.utils

import com.ravi.mylivetv.domain.model.Channel

/**
 * Temporary holder for channel list during navigation to PlayerScreen
 * This avoids passing large lists through navigation arguments
 */
object ChannelListHolder {
    private var channelList: List<Channel> = emptyList()
    private var categoryName: String = ""
    
    fun setChannels(channels: List<Channel>, category: String) {
        channelList = channels
        categoryName = category
    }
    
    fun getChannels(): List<Channel> = channelList
    
    fun getCategory(): String = categoryName
    
    fun clear() {
        channelList = emptyList()
        categoryName = ""
    }
}



