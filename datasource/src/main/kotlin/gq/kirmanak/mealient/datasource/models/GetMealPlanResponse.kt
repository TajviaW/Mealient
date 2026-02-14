package gq.kirmanak.mealient.datasource.models

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class GetMealPlanResponse(
    @SerialName("id") val id: String,
    @SerialName("date") val date: String, // ISO 8601 date: "2024-02-14"
    @SerialName("entryType") val entryType: String, // "breakfast", "lunch", "dinner", "snack"
    @SerialName("title") val title: String? = null,
    @SerialName("text") val text: String? = null,
    @SerialName("recipeId") val recipeId: String? = null,
    @SerialName("recipe") val recipe: GetRecipeSummaryResponse? = null,
    @SerialName("householdId") val householdId: String,
)
