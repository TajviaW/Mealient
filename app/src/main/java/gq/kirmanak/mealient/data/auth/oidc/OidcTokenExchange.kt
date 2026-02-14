package gq.kirmanak.mealient.data.auth.oidc

/**
 * Handles OAuth2/OIDC token exchange operations.
 */
interface OidcTokenExchange {
    /**
     * Exchanges an authorization code for access and refresh tokens.
     *
     * @param config The OIDC configuration
     * @param authorizationCode The authorization code from the OAuth provider
     * @param codeVerifier The PKCE code verifier used in the authorization request
     * @return Success with OidcTokens if exchange succeeds, Failure otherwise
     */
    suspend fun exchangeCodeForTokens(
        config: OidcConfig,
        authorizationCode: String,
        codeVerifier: String,
    ): Result<OidcTokens>

    /**
     * Refreshes an access token using a refresh token.
     *
     * @param config The OIDC configuration
     * @param refreshToken The refresh token to use
     * @return Success with new OidcTokens if refresh succeeds, Failure otherwise
     */
    suspend fun refreshAccessToken(
        config: OidcConfig,
        refreshToken: String,
    ): Result<OidcTokens>
}
