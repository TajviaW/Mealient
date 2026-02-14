package gq.kirmanak.mealient.ui.components

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
import androidx.paging.LoadState
import androidx.paging.compose.LazyPagingItems

/**
 * Adaptive grid with pull-to-refresh for paging data.
 * Automatically adjusts columns based on the provided column count.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun <T : Any> LazyPagingGridPullRefresh(
    lazyPagingItems: LazyPagingItems<T>,
    columns: Int,
    modifier: Modifier = Modifier,
    contentPadding: PaddingValues = PaddingValues(0.dp),
    verticalArrangement: Arrangement.Vertical = Arrangement.Top,
    horizontalArrangement: Arrangement.Horizontal = Arrangement.Start,
    lazyGridContent: LazyGridScope.() -> Unit,
) {
    val isRefreshing = lazyPagingItems.loadState.refresh is LoadState.Loading
    val gridState = rememberLazyGridState()

    PullToRefreshBox(
        isRefreshing = isRefreshing,
        onRefresh = lazyPagingItems::refresh,
        modifier = modifier,
    ) {
        LazyVerticalGrid(
            columns = GridCells.Fixed(columns),
            modifier = Modifier.fillMaxSize(),
            state = gridState,
            contentPadding = contentPadding,
            verticalArrangement = verticalArrangement,
            horizontalArrangement = horizontalArrangement,
            content = lazyGridContent,
        )
    }
}
