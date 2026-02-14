package gq.kirmanak.mealient.datasource.models

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class VersionResponse(
    @SerialName("version") val version: String,
    @SerialName("production") val production: Boolean? = null,
    @SerialName("demoStatus") val demoStatus: Boolean? = null,
    @SerialName("allowSignup") val allowSignup: Boolean? = null,
)