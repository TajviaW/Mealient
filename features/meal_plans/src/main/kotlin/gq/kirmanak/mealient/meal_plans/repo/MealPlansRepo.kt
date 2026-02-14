package gq.kirmanak.mealient.meal_plans.repo

import gq.kirmanak.mealient.datasource.models.GetMealPlanResponse

interface MealPlansRepo {
    suspend fun getMealPlansForDateRange(startDate: String, endDate: String): List<GetMealPlanResponse>
    suspend fun createMealPlan(date: String, entryType: String, recipeId: String?, title: String?, text: String?)
    suspend fun updateMealPlan(id: String, date: String, entryType: String, recipeId: String?, title: String?, text: String?)
    suspend fun deleteMealPlan(id: String)
}
