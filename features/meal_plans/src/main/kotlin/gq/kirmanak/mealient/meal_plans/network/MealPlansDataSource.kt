package gq.kirmanak.mealient.meal_plans.network

import gq.kirmanak.mealient.datasource.models.CreateMealPlanRequest
import gq.kirmanak.mealient.datasource.models.GetMealPlanResponse
import gq.kirmanak.mealient.datasource.models.UpdateMealPlanRequest

interface MealPlansDataSource {
    suspend fun getMealPlans(startDate: String, endDate: String): List<GetMealPlanResponse>
    suspend fun createMealPlan(request: CreateMealPlanRequest): GetMealPlanResponse
    suspend fun updateMealPlan(id: String, request: UpdateMealPlanRequest): GetMealPlanResponse
    suspend fun deleteMealPlan(id: String)
}
