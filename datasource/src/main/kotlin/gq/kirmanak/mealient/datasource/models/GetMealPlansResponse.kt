package gq.kirmanak.mealient.datasource.models

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class GetMealPlansResponse(
    @SerialName("items") val items: List<GetMealPlanResponse>,
    @SerialName("total") val total: Int,
    @SerialName("page") val page: Int? = null,
    @SerialName("perPage") val perPage: Int? = null,
)
