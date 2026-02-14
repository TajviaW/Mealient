package gq.kirmanak.mealient.datasource

import gq.kirmanak.mealient.datasource.models.CreateApiTokenRequest
import gq.kirmanak.mealient.datasource.models.CreateApiTokenResponse
import gq.kirmanak.mealient.datasource.models.CreateMealPlanRequest
import gq.kirmanak.mealient.datasource.models.CreateRecipeRequest
import gq.kirmanak.mealient.datasource.models.CreateShoppingListItemRequest
import gq.kirmanak.mealient.datasource.models.CreateShoppingListRequest
import gq.kirmanak.mealient.datasource.models.GetFoodsResponse
import gq.kirmanak.mealient.datasource.models.GetMealPlanResponse
import gq.kirmanak.mealient.datasource.models.GetMealPlansResponse
import gq.kirmanak.mealient.datasource.models.GetRecipeResponse
import gq.kirmanak.mealient.datasource.models.GetRecipesResponse
import gq.kirmanak.mealient.datasource.models.GetShoppingListResponse
import gq.kirmanak.mealient.datasource.models.GetShoppingListsResponse
import gq.kirmanak.mealient.datasource.models.GetTokenResponse
import gq.kirmanak.mealient.datasource.models.GetUnitsResponse
import gq.kirmanak.mealient.datasource.models.GetUserInfoResponse
import gq.kirmanak.mealient.datasource.models.ParseRecipeURLRequest
import gq.kirmanak.mealient.datasource.models.UpdateMealPlanRequest
import gq.kirmanak.mealient.datasource.models.UpdateRecipeRequest
import gq.kirmanak.mealient.datasource.models.VersionResponse
import kotlinx.serialization.json.JsonElement

internal interface MealieService {

    suspend fun getToken(username: String, password: String): GetTokenResponse

    suspend fun createRecipe(addRecipeRequest: CreateRecipeRequest): String

    suspend fun updateRecipe(
        addRecipeRequest: UpdateRecipeRequest,
        slug: String,
    ): GetRecipeResponse

    suspend fun getVersion(baseURL: String): VersionResponse

    suspend fun getRecipeSummary(page: Int, perPage: Int): GetRecipesResponse

    suspend fun getRecipe(slug: String): GetRecipeResponse

    suspend fun createRecipeFromURL(request: ParseRecipeURLRequest): String

    suspend fun createApiToken(request: CreateApiTokenRequest): CreateApiTokenResponse

    suspend fun getUserSelfInfo(): GetUserInfoResponse

    suspend fun removeFavoriteRecipe(userId: String, recipeSlug: String)

    suspend fun addFavoriteRecipe(userId: String, recipeSlug: String)

    suspend fun deleteRecipe(slug: String)

    suspend fun getShoppingLists(page: Int, perPage: Int): GetShoppingListsResponse

    suspend fun getShoppingList(id: String): GetShoppingListResponse

    suspend fun getShoppingListItem(id: String): JsonElement

    suspend fun updateShoppingListItem(id: String, request: JsonElement)

    suspend fun deleteShoppingListItem(id: String)

    suspend fun getFoods(perPage: Int): GetFoodsResponse

    suspend fun getUnits(perPage: Int): GetUnitsResponse

    suspend fun createShoppingListItem(request: CreateShoppingListItemRequest)

    suspend fun createShoppingList(request: CreateShoppingListRequest)

    suspend fun deleteShoppingList(id: String)

    suspend fun updateShoppingList(id: String, request: JsonElement)

    suspend fun getShoppingListJson(id: String) : JsonElement

    suspend fun getMealPlans(startDate: String, endDate: String): GetMealPlansResponse

    suspend fun getMealPlan(id: String): GetMealPlanResponse

    suspend fun createMealPlan(request: CreateMealPlanRequest): GetMealPlanResponse

    suspend fun updateMealPlan(id: String, request: UpdateMealPlanRequest): GetMealPlanResponse

    suspend fun deleteMealPlan(id: String)
}