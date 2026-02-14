package gq.kirmanak.mealient.meal_plans.network

import gq.kirmanak.mealient.datasource.MealieDataSource
import gq.kirmanak.mealient.datasource.models.CreateMealPlanRequest
import gq.kirmanak.mealient.datasource.models.GetMealPlanResponse
import gq.kirmanak.mealient.datasource.models.UpdateMealPlanRequest
import javax.inject.Inject

class MealPlansDataSourceImpl @Inject constructor(
    private val dataSource: MealieDataSource,
) : MealPlansDataSource {

    override suspend fun getMealPlans(startDate: String, endDate: String): List<GetMealPlanResponse> =
        dataSource.getMealPlans(startDate, endDate).items

    override suspend fun createMealPlan(request: CreateMealPlanRequest): GetMealPlanResponse =
        dataSource.createMealPlan(request)

    override suspend fun updateMealPlan(id: String, request: UpdateMealPlanRequest): GetMealPlanResponse =
        dataSource.updateMealPlan(id, request)

    override suspend fun deleteMealPlan(id: String) =
        dataSource.deleteMealPlan(id)
}
