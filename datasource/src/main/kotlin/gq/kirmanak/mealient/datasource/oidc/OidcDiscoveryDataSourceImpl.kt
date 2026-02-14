package gq.kirmanak.mealient.datasource.oidc

import gq.kirmanak.mealient.logging.Logger
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.get
import io.ktor.http.isSuccess
import javax.inject.Inject

/**
 * Implementation of OidcDiscoveryDataSource using Ktor HttpClient.
 */
internal class OidcDiscoveryDataSourceImpl @Inject constructor(
    private val httpClient: HttpClient,
    private val logger: Logger,
) : OidcDiscoveryDataSource {

    override suspend fun checkOidcEnabled(baseUrl: String): Boolean {
        return try {
            val response = httpClient.get("$baseUrl/.well-known/openid-configuration")
            val isSuccess = response.status.isSuccess()
            logger.v { "checkOidcEnabled: baseUrl=$baseUrl, isSuccess=$isSuccess" }
            isSuccess
        } catch (e: Exception) {
            logger.d(e) { "OIDC not available at $baseUrl" }
            false
        }
    }

    override suspend fun discoverConfiguration(baseUrl: String): OidcDiscoveryResponse {
        logger.v { "discoverConfiguration: baseUrl=$baseUrl" }
        val response = httpClient.get("$baseUrl/.well-known/openid-configuration").body<OidcDiscoveryResponse>()
        logger.v { "discoverConfiguration: response=$response" }
        return response
    }
}
