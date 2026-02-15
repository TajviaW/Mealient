package gq.kirmanak.mealient.data.auth.oidc

import gq.kirmanak.mealient.data.auth.AuthStorage
import gq.kirmanak.mealient.logging.Logger
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Handles refreshing OIDC access tokens using refresh tokens.
 */
interface OidcTokenRefresh {
    /**
     * Refreshes the OIDC access token if needed.
     *
     * @return New OidcTokens if refresh succeeds, null if refresh fails or not configured
     */
    suspend fun refreshIfNeeded(): OidcTokens?
}

/**
 * Implementation of OidcTokenRefresh.
 */
@Singleton
class OidcTokenRefreshImpl @Inject constructor(
    private val authStorage: AuthStorage,
    private val oidcAuthRepo: OidcAuthRepo,
    private val oidcTokenExchange: OidcTokenExchange,
    private val logger: Logger,
) : OidcTokenRefresh {

    override suspend fun refreshIfNeeded(): OidcTokens? {
        logger.v { "refreshIfNeeded" }

        val refreshToken = authStorage.getOidcRefreshToken()
        if (refreshToken == null) {
            logger.w { "No refresh token available" }
            return null
        }

        val configState = oidcAuthRepo.oidcAuthState.value
        if (configState !is OidcAuthState.Configured) {
            logger.w { "OIDC not configured" }
            return null
        }

        return oidcTokenExchange.refreshAccessToken(
            config = configState.config,
            refreshToken = refreshToken
        ).fold(
            onSuccess = { tokens ->
                logger.v { "Token refresh successful" }
                // Store the new tokens
                authStorage.setOidcTokens(
                    accessToken = tokens.accessToken,
                    refreshToken = tokens.refreshToken,
                    idToken = tokens.idToken
                )
                tokens
            },
            onFailure = { error ->
                logger.e(error) { "Token refresh failed" }
                null
            }
        )
    }
}
