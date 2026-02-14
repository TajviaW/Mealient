package gq.kirmanak.mealient.datasource.models

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class CreateMealPlanRequest(
    @SerialName("date") val date: String,
    @SerialName("entryType") val entryType: String,
    @SerialName("recipeId") val recipeId: String? = null,
    @SerialName("title") val title: String? = null,
    @SerialName("text") val text: String? = null,
)
