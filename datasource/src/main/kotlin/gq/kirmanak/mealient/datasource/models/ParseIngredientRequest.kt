package gq.kirmanak.mealient.datasource.models

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class ParseIngredientRequest(
    @SerialName("parser") val parser: String = "nlp",
    @SerialName("ingredient") val ingredient: String,
)

@Serializable
data class ParsedIngredientResponse(
    @SerialName("ingredient") val ingredient: ParsedIngredient,
)

@Serializable
data class ParsedIngredient(
    @SerialName("note") val note: String = "",
    @SerialName("unit") val unit: GetUnitResponse? = null,
    @SerialName("food") val food: GetFoodResponse? = null,
    @SerialName("quantity") val quantity: Double? = null,
    @SerialName("display") val display: String = "",
    @SerialName("referenceId") val referenceId: String = "",
    @SerialName("title") val title: String? = null,
    @SerialName("isFood") val isFood: Boolean = false,
    @SerialName("disableAmount") val disableAmount: Boolean = false,
)
