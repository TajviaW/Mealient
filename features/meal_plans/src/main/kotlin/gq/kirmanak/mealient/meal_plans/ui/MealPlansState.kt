package gq.kirmanak.mealient.meal_plans.ui

import gq.kirmanak.mealient.datasource.models.GetMealPlanResponse
import gq.kirmanak.mealient.datasource.models.GetRecipeSummaryResponse

sealed class MealPlansLoadingState {
    object Loading : MealPlansLoadingState()
    data class Success(val mealPlans: List<GetMealPlanResponse>) : MealPlansLoadingState()
    data class Error(val message: String) : MealPlansLoadingState()
}

data class MealPlansState(
    val loadingState: MealPlansLoadingState = MealPlansLoadingState.Loading,
    val startDate: String = "",
    val endDate: String = "",
    val isRefreshing: Boolean = false,
    val showMealPlanDialog: Boolean = false,
    val editingMealPlanId: Int? = null,
    val availableRecipes: List<GetRecipeSummaryResponse> = emptyList(),
    val isLoadingRecipes: Boolean = false,
    val showMealPlanOptionsDialog: Boolean = false,
    val selectedMealPlanForOptions: GetMealPlanResponse? = null,
)
