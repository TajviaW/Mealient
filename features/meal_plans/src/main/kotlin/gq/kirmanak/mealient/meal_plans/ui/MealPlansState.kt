package gq.kirmanak.mealient.meal_plans.ui

import gq.kirmanak.mealient.datasource.models.GetMealPlanResponse

sealed class MealPlansLoadingState {
    object Loading : MealPlansLoadingState()
    data class Success(val mealPlans: List<GetMealPlanResponse>) : MealPlansLoadingState()
    data class Error(val message: String) : MealPlansLoadingState()
}

data class MealPlansState(
    val loadingState: MealPlansLoadingState = MealPlansLoadingState.Loading,
    val startDate: String = "",
    val endDate: String = "",
)
