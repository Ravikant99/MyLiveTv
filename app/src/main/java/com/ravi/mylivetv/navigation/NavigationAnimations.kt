package com.ravi.mylivetv.navigation

import androidx.compose.animation.AnimatedContentTransitionScope
import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.navigation.NavBackStackEntry

/**
 * Navigation animation configurations for consistent screen transitions
 */
object NavigationAnimations {
    
    private const val SLIDE_DURATION = 400
    private const val SCALE_DURATION = 500
    
    // Horizontal slide animations (for list-like navigation)
    fun slideInFromRight(): AnimatedContentTransitionScope<NavBackStackEntry>.() -> EnterTransition = {
        slideIntoContainer(
            towards = AnimatedContentTransitionScope.SlideDirection.Left,
            animationSpec = tween(SLIDE_DURATION)
        ) + fadeIn(animationSpec = tween(SLIDE_DURATION))
    }
    
    fun slideOutToLeft(): AnimatedContentTransitionScope<NavBackStackEntry>.() -> ExitTransition = {
        slideOutOfContainer(
            towards = AnimatedContentTransitionScope.SlideDirection.Left,
            animationSpec = tween(SLIDE_DURATION)
        ) + fadeOut(animationSpec = tween(SLIDE_DURATION))
    }
    
    fun slideInFromLeft(): AnimatedContentTransitionScope<NavBackStackEntry>.() -> EnterTransition = {
        slideIntoContainer(
            towards = AnimatedContentTransitionScope.SlideDirection.Right,
            animationSpec = tween(SLIDE_DURATION)
        ) + fadeIn(animationSpec = tween(SLIDE_DURATION))
    }
    
    fun slideOutToRight(): AnimatedContentTransitionScope<NavBackStackEntry>.() -> ExitTransition = {
        slideOutOfContainer(
            towards = AnimatedContentTransitionScope.SlideDirection.Right,
            animationSpec = tween(SLIDE_DURATION)
        ) + fadeOut(animationSpec = tween(SLIDE_DURATION))
    }
    
    // Scale animations (for modal-like transitions, e.g., video player)
    fun scaleInWithFade(): AnimatedContentTransitionScope<NavBackStackEntry>.() -> EnterTransition = {
        scaleIn(
            initialScale = 0.9f,
            animationSpec = tween(SCALE_DURATION)
        ) + fadeIn(animationSpec = tween(SCALE_DURATION))
    }
    
    fun scaleOutWithFade(): AnimatedContentTransitionScope<NavBackStackEntry>.() -> ExitTransition = {
        scaleOut(
            targetScale = 0.9f,
            animationSpec = tween(SCALE_DURATION)
        ) + fadeOut(animationSpec = tween(SCALE_DURATION))
    }
}



