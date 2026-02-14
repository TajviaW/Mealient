package gq.kirmanak.mealient.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyGridScope
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.rememberLazyGridState
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FabPosition
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import gq.kirmanak.mealient.ui.util.LoadingState
import gq.kirmanak.mealient.ui.util.LoadingStateNoData
import gq.kirmanak.mealient.ui.util.data
import gq.kirmanak.mealient.ui.util.getGridColumns
import gq.kirmanak.mealient.ui.util.isLoading
import gq.kirmanak.mealient.ui.util.isRefreshing
import gq.kirmanak.mealient.ui.util.rememberWindowSize

/**
 * Adaptive lazy column/grid with loading state support.
 * Uses a single column on phones and a grid on tablets.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun <T> AdaptiveLazyColumnWithLoadingState(
    loadingState: LoadingState<List<T>>,
    emptyListError: String,
    retryButtonText: String,
    modifier: Modifier = Modifier,
    contentPadding: PaddingValues = PaddingValues(0.dp),
    verticalArrangement: Arrangement.Vertical = Arrangement.Top,
    snackbarText: String?,
    onSnackbarShown: () -> Unit = {},
    onRefresh: () -> Unit = {},
    floatingActionButton: @Composable () -> Unit = {},
    floatingActionButtonPosition: FabPosition = FabPosition.End,
    lazyColumnContent: LazyListScope.(List<T>) -> Unit = {},
    lazyGridContent: LazyGridScope.(List<T>) -> Unit = {},
) {
    val windowSize = rememberWindowSize()
    val snackbarHostState = remember { SnackbarHostState() }

    Scaffold(
        modifier = modifier,
        floatingActionButton = floatingActionButton,
        floatingActionButtonPosition = floatingActionButtonPosition,
        snackbarHost = { SnackbarHost(snackbarHostState) },
    ) { paddingValues ->
        val innerModifier = Modifier
            .padding(paddingValues)
            .fillMaxSize()

        val list = loadingState.data ?: emptyList()

        when {
            loadingState is LoadingStateNoData.InitialLoad -> {
                CenteredProgressIndicator(modifier = innerModifier)
            }

            !loadingState.isLoading && list.isEmpty() -> {
                EmptyListError(
                    text = emptyListError,
                    retryButtonText = retryButtonText,
                    onRetry = onRefresh,
                    modifier = innerModifier,
                )
            }

            windowSize.isCompact -> {
                // Phone: use single column
                LazyColumnPullRefresh(
                    isRefreshing = loadingState.isRefreshing,
                    onRefresh = onRefresh,
                    contentPadding = contentPadding,
                    verticalArrangement = verticalArrangement,
                    lazyColumnContent = { lazyColumnContent(list) },
                    modifier = innerModifier,
                )
            }

            else -> {
                // Tablet: use grid
                val columns = windowSize.getGridColumns()
                PullToRefreshBox(
                    isRefreshing = loadingState.isRefreshing,
                    onRefresh = onRefresh,
                    modifier = innerModifier,
                ) {
                    LazyVerticalGrid(
                        columns = GridCells.Fixed(columns),
                        modifier = Modifier.fillMaxSize(),
                        state = rememberLazyGridState(),
                        contentPadding = contentPadding,
                        verticalArrangement = verticalArrangement,
                        horizontalArrangement = Arrangement.spacedBy(16.dp),
                        content = { lazyGridContent(list) },
                    )
                }
            }
        }

        ErrorSnackbar(
            text = snackbarText,
            snackbarHostState = snackbarHostState,
            onSnackbarShown = onSnackbarShown,
        )
    }
}
