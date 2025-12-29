package com.ravi.mylivetv.utils

data class HomeUiState(
    val selectedTab: Int = 0,
    val selectedItemIndex: Int = -1
)

data class PlayerUiState(
    val isBuffering: Boolean = true,
    val isPlaying: Boolean = false,
    val errorMessage: String? = null,
    val channelName: String = "",
    val channelLogoUrl: String = "",
    val hasStartedPlaying: Boolean = false, // Track if playback has started at least once
    val category: String = "",
    val categoryUrl: String = "",
    val hasNextChannel: Boolean = false,
    val hasPreviousChannel: Boolean = false
)