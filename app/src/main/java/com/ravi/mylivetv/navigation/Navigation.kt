package com.ravi.mylivetv.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.ravi.mylivetv.ui.channel.ChannelScreen
import com.ravi.mylivetv.ui.home.HomeScreen
import com.ravi.mylivetv.ui.player.PlayerScreen
import java.net.URLDecoder
import java.nio.charset.StandardCharsets

@Composable
fun Navigation() {
    val navController = rememberNavController()

    NavHost(
        navController = navController,
        startDestination = ScreenRoutes.HomeScreen.routes
    ) {
        // Home Screen - Entry point
        composable(
            route = ScreenRoutes.HomeScreen.routes,
            enterTransition = NavigationAnimations.slideInFromLeft(),
            exitTransition = NavigationAnimations.slideOutToLeft(),
            popEnterTransition = NavigationAnimations.slideInFromLeft(),
            popExitTransition = NavigationAnimations.slideOutToRight()
        ) {
            HomeScreen(navController)
        }
        
        // Channel Screen - Category/Language/Country listings
        composable(
            route = ScreenRoutes.ChannelScreen.routes,
            arguments = listOf(
                navArgument("category") { type = NavType.StringType }
            ),
            enterTransition = NavigationAnimations.slideInFromRight(),
            exitTransition = NavigationAnimations.slideOutToLeft(),
            popEnterTransition = NavigationAnimations.slideInFromRight(),
            popExitTransition = NavigationAnimations.slideOutToRight()
        ) { backStackEntry ->
            val category = backStackEntry.arguments?.getString("category") ?: ""
            ChannelScreen(navController, category)
        }
        
        // Player Screen - Video playback
        composable(
            route = ScreenRoutes.PlayerScreen.routes,
            arguments = listOf(
                navArgument("streamUrl") { type = NavType.StringType },
                navArgument("channelName") { type = NavType.StringType },
                navArgument("logoUrl") { type = NavType.StringType },
                navArgument("category") { type = NavType.StringType },
                navArgument("categoryUrl") { type = NavType.StringType },
                navArgument("channelIndex") { 
                    type = NavType.IntType
                    defaultValue = -1
                }
            ),
            enterTransition = NavigationAnimations.scaleInWithFade(),
            exitTransition = NavigationAnimations.scaleOutWithFade(),
            popEnterTransition = NavigationAnimations.scaleInWithFade(),
            popExitTransition = NavigationAnimations.scaleOutWithFade()
        ) { backStackEntry ->
            val encodedUrl = backStackEntry.arguments?.getString("streamUrl") ?: ""
            val encodedName = backStackEntry.arguments?.getString("channelName") ?: ""
            val encodedLogo = backStackEntry.arguments?.getString("logoUrl") ?: ""
            val encodedCategory = backStackEntry.arguments?.getString("category") ?: ""
            val encodedCategoryUrl = backStackEntry.arguments?.getString("categoryUrl") ?: ""
            val channelIndex = backStackEntry.arguments?.getInt("channelIndex") ?: -1
            
            val streamUrl = URLDecoder.decode(encodedUrl, StandardCharsets.UTF_8.toString())
            val channelName = URLDecoder.decode(encodedName, StandardCharsets.UTF_8.toString())
            val logoUrl = URLDecoder.decode(encodedLogo, StandardCharsets.UTF_8.toString())
            val category = URLDecoder.decode(encodedCategory, StandardCharsets.UTF_8.toString())
            val categoryUrl = URLDecoder.decode(encodedCategoryUrl, StandardCharsets.UTF_8.toString())
            
            PlayerScreen(navController, streamUrl, channelName, logoUrl, category, categoryUrl, channelIndex)
        }
    }
}