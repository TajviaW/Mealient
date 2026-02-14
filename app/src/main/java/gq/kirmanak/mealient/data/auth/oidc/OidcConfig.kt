package gq.kirmanak.mealient.data.auth.oidc

/**
 * OIDC configuration discovered from the server's .well-known/openid-configuration endpoint.
 *
 * @property authorizationEndpoint URL for the OAuth2 authorization endpoint
 * @property tokenEndpoint URL for the OAuth2 token endpoint
 * @property endSessionEndpoint Optional URL for the end session (logout) endpoint
 * @property issuer The OIDC issuer identifier
 * @property clientId The OAuth2 client ID to use for this app (defaults to "mealient-mobile")
 */
data class OidcConfig(
    val authorizationEndpoint: String,
    val tokenEndpoint: String,
    val endSessionEndpoint: String?,
    val issuer: String,
    val clientId: String = "mealient-mobile",
)

/**
 * Represents the state of OIDC authentication configuration.
 */
sealed class OidcAuthState {
    /**
     * OIDC has not been configured yet (initial state or server doesn't support OIDC)
     */
    data object NotConfigured : OidcAuthState()

    /**
     * OIDC configuration is being discovered
     */
    data object Pending : OidcAuthState()

    /**
     * OIDC configuration has been successfully discovered and is ready to use
     */
    data class Configured(val config: OidcConfig) : OidcAuthState()

    /**
     * OIDC configuration discovery failed
     */
    data class Failed(val error: String) : OidcAuthState()
}

/**
 * Represents an OIDC authorization request ready to be launched in a browser.
 *
 * @property authorizationUrl The complete authorization URL to open in Chrome Custom Tabs
 * @property state The state parameter for CSRF protection
 * @property codeVerifier The PKCE code verifier (stored for later token exchange)
 * @property authRequest The AppAuth AuthorizationRequest object (for launching intent)
 */
data class OidcAuthorizationRequest(
    val authorizationUrl: String,
    val state: String,
    val codeVerifier: String,
    val authRequest: net.openid.appauth.AuthorizationRequest,
)

/**
 * OIDC tokens received from the token endpoint.
 *
 * @property accessToken The access token for API requests
 * @property refreshToken Optional refresh token for obtaining new access tokens
 * @property idToken Optional ID token containing user identity claims
 * @property expiresIn Optional token expiration time in seconds
 */
data class OidcTokens(
    val accessToken: String,
    val refreshToken: String?,
    val idToken: String?,
    val expiresIn: Long? = null,
)
