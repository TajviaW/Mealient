package gq.kirmanak.mealient.meal_plans.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.ramcosta.composedestinations.annotation.Destination
import gq.kirmanak.mealient.datasource.models.GetMealPlanResponse
import gq.kirmanak.mealient.meal_plans.R
import gq.kirmanak.mealient.ui.components.BaseScreenState
import gq.kirmanak.mealient.ui.components.BaseScreenWithNavigation

@Destination
@Composable
fun MealPlansScreen(
    navController: NavController,
    baseScreenState: BaseScreenState,
    viewModel: MealPlansViewModel = hiltViewModel(),
) {
    val state by viewModel.state.collectAsState()

    BaseScreenWithNavigation(
        baseScreenState = baseScreenState,
    ) { modifier ->
        Column(modifier = modifier.fillMaxSize()) {
            when (val loadingState = state.loadingState) {
                is MealPlansLoadingState.Loading -> {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator()
                    }
                }
                is MealPlansLoadingState.Error -> {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = loadingState.message,
                            color = MaterialTheme.colorScheme.error
                        )
                    }
                }
                is MealPlansLoadingState.Success -> {
                    MealPlansContent(
                        modifier = Modifier,
                        mealPlans = loadingState.mealPlans,
                        startDate = state.startDate,
                        endDate = state.endDate,
                        isRefreshing = state.isRefreshing,
                        onRefresh = viewModel::onRefresh
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun MealPlansContent(
    modifier: Modifier = Modifier,
    mealPlans: List<GetMealPlanResponse>,
    startDate: String,
    endDate: String,
    isRefreshing: Boolean,
    onRefresh: () -> Unit,
) {
    PullToRefreshBox(
        isRefreshing = isRefreshing,
        onRefresh = onRefresh,
        modifier = modifier.fillMaxSize(),
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            // Header showing date range
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                    Icons.Default.CalendarToday,
                    contentDescription = stringResource(R.string.meal_plans_content_desc_calendar_icon)
                )
                    Text(
                        text = stringResource(R.string.meal_plans_date_range_format, startDate, endDate),
                        style = MaterialTheme.typography.titleMedium
                    )
                }
            }

            if (mealPlans.isEmpty()) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(
                            text = stringResource(R.string.meal_plans_screen_empty),
                            style = MaterialTheme.typography.titleMedium
                        )
                        Text(
                            text = stringResource(R.string.meal_plans_screen_empty_subtitle),
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(mealPlans) { mealPlan ->
                        MealPlanCard(mealPlan = mealPlan)
                    }
                }
            }
        }
    }
}

@Composable
private fun MealPlanCard(mealPlan: GetMealPlanResponse) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Text(
                text = mealPlan.date,
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.primary
            )
            Text(
                text = mealPlan.entryType.replaceFirstChar { it.uppercase() },
                style = MaterialTheme.typography.titleMedium
            )
            mealPlan.recipe?.let { recipe ->
                Text(
                    text = recipe.name,
                    style = MaterialTheme.typography.bodyLarge
                )
            }
            mealPlan.title?.let { title ->
                Text(
                    text = title,
                    style = MaterialTheme.typography.bodyMedium
                )
            }
            mealPlan.text?.let { text ->
                Text(
                    text = text,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}
