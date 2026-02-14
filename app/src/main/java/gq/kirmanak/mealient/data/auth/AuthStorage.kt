package gq.kirmanak.mealient.data.auth

import kotlinx.coroutines.flow.Flow

/**
 * Authentication methods supported by the app.
 */
enum class AuthMethod {
    PASSWORD,
    OIDC,
    NONE
}

interface AuthStorage {

    val authTokenFlow: Flow<String?>

    val authMethodFlow: Flow<AuthMethod>

    suspend fun setAuthToken(authToken: String?)

    suspend fun getAuthToken(): String?

    /**
     * Sets the authentication token for password-based authentication.
     */
    suspend fun setPasswordAuthToken(authToken: String)

    /**
     * Stores OIDC tokens securely.
     *
     * @param accessToken The access token
     * @param refreshToken The refresh token (optional)
     * @param idToken The ID token (optional)
     */
    suspend fun setOidcTokens(
        accessToken: String,
        refreshToken: String?,
        idToken: String?,
    )

    /**
     * Gets the stored OIDC refresh token.
     */
    suspend fun getOidcRefreshToken(): String?

    /**
     * Gets the stored OIDC ID token.
     */
    suspend fun getOidcIdToken(): String?

    /**
     * Clears all OIDC tokens from storage.
     */
    suspend fun clearOidcTokens()
}