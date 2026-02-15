package gq.kirmanak.mealient.datasource.models

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class VersionResponse(
    @SerialName("version") val version: String,
    @SerialName("production") val production: Boolean? = null,
    @SerialName("demoStatus") val demoStatus: Boolean? = null,
    @SerialName("allowSignup") val allowSignup: Boolean? = null,
    @SerialName("allowPasswordLogin") val allowPasswordLogin: Boolean? = null,
    @SerialName("enableOidc") val enableOidc: Boolean? = null,
    @SerialName("oidcRedirect") val oidcRedirect: Boolean? = null,
    @SerialName("oidcProviderName") val oidcProviderName: String? = null,
)