package gq.kirmanak.mealient.meal_plans.repo

import gq.kirmanak.mealient.datasource.models.CreateMealPlanRequest
import gq.kirmanak.mealient.datasource.models.GetMealPlanResponse
import gq.kirmanak.mealient.datasource.models.UpdateMealPlanRequest
import gq.kirmanak.mealient.logging.Logger
import gq.kirmanak.mealient.meal_plans.network.MealPlansDataSource
import javax.inject.Inject

class MealPlansRepoImpl @Inject constructor(
    private val dataSource: MealPlansDataSource,
    private val logger: Logger,
) : MealPlansRepo {

    override suspend fun getMealPlansForDateRange(startDate: String, endDate: String): List<GetMealPlanResponse> {
        logger.v { "getMealPlansForDateRange: startDate=$startDate, endDate=$endDate" }
        return dataSource.getMealPlans(startDate, endDate)
    }

    override suspend fun createMealPlan(
        date: String,
        entryType: String,
        recipeId: String?,
        title: String?,
        text: String?
    ) {
        logger.v { "createMealPlan: date=$date, entryType=$entryType, recipeId=$recipeId" }
        val request = CreateMealPlanRequest(
            date = date,
            entryType = entryType,
            recipeId = recipeId,
            title = title,
            text = text
        )
        dataSource.createMealPlan(request)
    }

    override suspend fun updateMealPlan(
        id: String,
        date: String,
        entryType: String,
        recipeId: String?,
        title: String?,
        text: String?
    ) {
        logger.v { "updateMealPlan: id=$id, date=$date, entryType=$entryType" }
        val request = UpdateMealPlanRequest(
            date = date,
            entryType = entryType,
            recipeId = recipeId,
            title = title,
            text = text
        )
        dataSource.updateMealPlan(id, request)
    }

    override suspend fun deleteMealPlan(id: String) {
        logger.v { "deleteMealPlan: id=$id" }
        dataSource.deleteMealPlan(id)
    }
}
