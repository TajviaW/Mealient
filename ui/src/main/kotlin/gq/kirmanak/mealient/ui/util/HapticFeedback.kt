package gq.kirmanak.mealient.ui.util

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.hapticfeedback.HapticFeedback
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback

/**
 * Provides haptic feedback for user interactions.
 * Wraps the platform haptic feedback with convenient methods for common patterns.
 */
interface HapticFeedbackProvider {
    /**
     * Light tap feedback for simple actions like button clicks.
     */
    fun lightImpact()

    /**
     * Medium impact feedback for important actions like favorites or confirmations.
     */
    fun mediumImpact()

    /**
     * Strong impact feedback for destructive actions like deletes.
     */
    fun heavyImpact()

    /**
     * Success feedback for completed operations.
     */
    fun success()

    /**
     * Error feedback for failed operations.
     */
    fun error()
}

internal class HapticFeedbackProviderImpl(
    private val hapticFeedback: HapticFeedback
) : HapticFeedbackProvider {

    override fun lightImpact() {
        hapticFeedback.performHapticFeedback(HapticFeedbackType.TextHandleMove)
    }

    override fun mediumImpact() {
        hapticFeedback.performHapticFeedback(HapticFeedbackType.LongPress)
    }

    override fun heavyImpact() {
        hapticFeedback.performHapticFeedback(HapticFeedbackType.LongPress)
    }

    override fun success() {
        hapticFeedback.performHapticFeedback(HapticFeedbackType.LongPress)
    }

    override fun error() {
        hapticFeedback.performHapticFeedback(HapticFeedbackType.LongPress)
    }
}

/**
 * Composable function to get a HapticFeedbackProvider instance.
 */
@Composable
fun rememberHapticFeedback(): HapticFeedbackProvider {
    val hapticFeedback = LocalHapticFeedback.current
    return remember(hapticFeedback) {
        HapticFeedbackProviderImpl(hapticFeedback)
    }
}
