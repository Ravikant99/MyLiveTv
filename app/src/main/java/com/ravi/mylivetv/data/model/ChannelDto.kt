package com.ravi.mylivetv.data.model

import com.ravi.mylivetv.domain.model.Channel

data class ChannelDto(
    val name: String,
    val logo: String,
    val streamUrl: String,
    val category: String
)

fun ChannelDto.toDomain(): Channel {
    return Channel(
        name = name,
        logo = logo,
        streamUrl = streamUrl,
        category = category
    )
}

