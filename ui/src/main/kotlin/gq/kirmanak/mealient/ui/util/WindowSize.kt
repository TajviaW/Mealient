package gq.kirmanak.mealient.ui.util

import androidx.compose.material3.adaptive.currentWindowAdaptiveInfo
import androidx.compose.runtime.Composable
import androidx.window.core.layout.WindowSizeClass
import androidx.window.core.layout.WindowWidthSizeClass

/**
 * Represents the current window size class for adaptive layouts.
 */
data class WindowSize(
    val widthSizeClass: WindowWidthSizeClass,
    val windowSizeClass: WindowSizeClass,
) {
    /**
     * True if the window is compact width (< 600dp), typically phones in portrait.
     */
    val isCompact: Boolean
        get() = widthSizeClass == WindowWidthSizeClass.COMPACT

    /**
     * True if the window is medium width (600dp - 840dp), typically tablets in portrait or phones in landscape.
     */
    val isMedium: Boolean
        get() = widthSizeClass == WindowWidthSizeClass.MEDIUM

    /**
     * True if the window is expanded width (> 840dp), typically tablets in landscape or desktops.
     */
    val isExpanded: Boolean
        get() = widthSizeClass == WindowWidthSizeClass.EXPANDED

    /**
     * True if the window is medium or expanded (tablet or larger).
     */
    val isTabletOrLarger: Boolean
        get() = isMedium || isExpanded
}

/**
 * Composable function to get the current window size.
 */
@Composable
fun rememberWindowSize(): WindowSize {
    val windowAdaptiveInfo = currentWindowAdaptiveInfo()
    return WindowSize(
        widthSizeClass = windowAdaptiveInfo.windowSizeClass.windowWidthSizeClass,
        windowSizeClass = windowAdaptiveInfo.windowSizeClass,
    )
}
