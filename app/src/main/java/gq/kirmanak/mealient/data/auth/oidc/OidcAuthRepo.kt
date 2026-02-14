package gq.kirmanak.mealient.data.auth.oidc

import kotlinx.coroutines.flow.StateFlow

/**
 * Repository for managing OIDC authentication flows.
 */
interface OidcAuthRepo {
    /**
     * Current state of OIDC authentication configuration.
     */
    val oidcAuthState: StateFlow<OidcAuthState>

    /**
     * Discovers OIDC configuration from the server's .well-known/openid-configuration endpoint.
     *
     * @param baseUrl The base URL of the Mealie server
     * @return Success with OidcConfig if discovery succeeds, Failure otherwise
     */
    suspend fun discoverOidcConfig(baseUrl: String): Result<OidcConfig>

    /**
     * Starts the OIDC authorization flow by building an authorization request.
     *
     * @return Success with OidcAuthorizationRequest containing the auth URL and PKCE parameters,
     *         Failure if OIDC is not configured
     */
    suspend fun startOidcFlow(): Result<OidcAuthorizationRequest>

    /**
     * Handles the authorization response callback with the authorization code.
     *
     * @param authorizationCode The authorization code from the OAuth provider
     * @param state The state parameter for CSRF validation
     * @param codeVerifier The PKCE code verifier stored from the authorization request
     * @return Success if token exchange and storage succeeds, Failure otherwise
     */
    suspend fun handleAuthorizationResponse(
        authorizationCode: String,
        state: String,
        codeVerifier: String,
    ): Result<Unit>
}
