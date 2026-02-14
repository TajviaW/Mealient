package gq.kirmanak.mealient.ui.util

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyGridScope
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.rememberLazyGridState
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

/**
 * Get the recommended number of grid columns based on window size.
 */
fun WindowSize.getGridColumns(): Int = when {
    isCompact -> 1  // Phone portrait: 1 column
    isMedium -> 2   // Tablet portrait or phone landscape: 2 columns
    isExpanded -> 3 // Tablet landscape or desktop: 3 columns
    else -> 1
}

/**
 * Adaptive grid that adjusts columns based on window size.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdaptiveGrid(
    windowSize: WindowSize,
    modifier: Modifier = Modifier,
    isRefreshing: Boolean = false,
    onRefresh: () -> Unit = {},
    contentPadding: PaddingValues = PaddingValues(16.dp),
    verticalArrangement: Arrangement.Vertical = Arrangement.spacedBy(16.dp),
    horizontalArrangement: Arrangement.Horizontal = Arrangement.spacedBy(16.dp),
    content: LazyGridScope.() -> Unit,
) {
    val columns = windowSize.getGridColumns()
    val gridState = rememberLazyGridState()

    PullToRefreshBox(
        isRefreshing = isRefreshing,
        onRefresh = onRefresh,
        modifier = modifier.fillMaxSize(),
    ) {
        LazyVerticalGrid(
            columns = GridCells.Fixed(columns),
            modifier = Modifier.fillMaxSize(),
            state = gridState,
            contentPadding = contentPadding,
            verticalArrangement = verticalArrangement,
            horizontalArrangement = horizontalArrangement,
            content = content,
        )
    }
}
