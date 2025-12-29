package com.ravi.mylivetv.navigation

import java.net.URLEncoder
import java.nio.charset.StandardCharsets

sealed interface ScreenRoutes {
    val routes: String

    object HomeScreen: ScreenRoutes {
        override val routes = "homeScreen"
    }

    object ChannelScreen: ScreenRoutes {
        override val routes = "channelScreen/{category}"
        
        fun createRoute(category: String): String {
            return "channelScreen/$category"
        }
    }

    object PlayerScreen: ScreenRoutes {
        override val routes = "playerScreen/{streamUrl}/{channelName}/{logoUrl}/{category}/{categoryUrl}/{channelIndex}"
        
        fun createRoute(
            streamUrl: String, 
            channelName: String = "", 
            logoUrl: String = "",
            category: String = "",
            categoryUrl: String = "",
            channelIndex: Int = -1
        ): String {
            val encodedUrl = URLEncoder.encode(streamUrl, StandardCharsets.UTF_8.toString())
            val encodedName = URLEncoder.encode(channelName, StandardCharsets.UTF_8.toString())
            val encodedLogo = URLEncoder.encode(logoUrl, StandardCharsets.UTF_8.toString())
            val encodedCategory = URLEncoder.encode(category, StandardCharsets.UTF_8.toString())
            val encodedCategoryUrl = URLEncoder.encode(categoryUrl, StandardCharsets.UTF_8.toString())
            return "playerScreen/$encodedUrl/$encodedName/$encodedLogo/$encodedCategory/$encodedCategoryUrl/$channelIndex"
        }
    }
}