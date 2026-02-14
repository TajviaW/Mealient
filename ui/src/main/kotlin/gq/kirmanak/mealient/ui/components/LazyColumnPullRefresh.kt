package gq.kirmanak.mealient.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LazyColumnPullRefresh(
    isRefreshing: Boolean,
    onRefresh: () -> Unit,
    contentPadding: PaddingValues,
    verticalArrangement: Arrangement.Vertical,
    lazyColumnContent: LazyListScope.() -> Unit,
    modifier: Modifier = Modifier,
    lazyListState: LazyListState = rememberLazyListState(),
) {
    PullToRefreshBox(
        isRefreshing = isRefreshing,
        onRefresh = onRefresh,
        modifier = modifier,
    ) {
        LazyColumn(
            contentPadding = contentPadding,
            verticalArrangement = verticalArrangement,
            state = lazyListState,
            content = lazyColumnContent
        )
    }
}