package gq.kirmanak.mealient.datasource

/**
 * Authentication methods supported by the provider.
 */
enum class AuthMethod {
    PASSWORD,
    OIDC,
    NONE
}

interface AuthenticationProvider {

    suspend fun getAuthToken(): String?

    suspend fun logout()

    /**
     * Gets the current authentication method.
     */
    suspend fun getAuthMethod(): AuthMethod

    /**
     * Refreshes the OIDC access token using the refresh token.
     * Returns null if not using OIDC or refresh fails.
     */
    suspend fun refreshOidcToken(): String?
}