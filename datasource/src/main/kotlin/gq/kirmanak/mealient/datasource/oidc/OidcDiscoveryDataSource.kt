package gq.kirmanak.mealient.datasource.oidc

import kotlinx.serialization.Serializable

/**
 * Data source for discovering OIDC configuration from a Mealie server.
 */
interface OidcDiscoveryDataSource {
    /**
     * Discovers the OIDC configuration from the server's .well-known/openid-configuration endpoint.
     *
     * @param baseUrl The base URL of the Mealie server
     * @return The discovered OIDC configuration
     * @throws Exception if discovery fails or the endpoint is not available
     */
    suspend fun discoverConfiguration(baseUrl: String): OidcDiscoveryResponse

    /**
     * Checks if OIDC is enabled on the server by attempting to access the discovery endpoint.
     *
     * @param baseUrl The base URL of the Mealie server
     * @return true if OIDC is available, false otherwise
     */
    suspend fun checkOidcEnabled(baseUrl: String): Boolean
}

/**
 * Response from the OIDC discovery endpoint (.well-known/openid-configuration).
 * This follows the OpenID Connect Discovery 1.0 specification.
 *
 * @property issuer The OIDC issuer identifier
 * @property authorization_endpoint URL of the OAuth 2.0 authorization endpoint
 * @property token_endpoint URL of the OAuth 2.0 token endpoint
 * @property end_session_endpoint Optional URL of the logout endpoint
 */
@Serializable
data class OidcDiscoveryResponse(
    val issuer: String,
    val authorization_endpoint: String,
    val token_endpoint: String,
    val end_session_endpoint: String? = null,
)
