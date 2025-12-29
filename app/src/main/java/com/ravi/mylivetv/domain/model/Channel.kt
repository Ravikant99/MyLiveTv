package com.ravi.mylivetv.domain.model

import android.media.tv.TvContract

data class Channel(
    val name: String,
    val logo: String,
    val streamUrl: String,
    val category: String
)
