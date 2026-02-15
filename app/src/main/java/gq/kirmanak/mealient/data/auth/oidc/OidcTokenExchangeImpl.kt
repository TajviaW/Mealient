package gq.kirmanak.mealient.data.auth.oidc

import android.net.Uri
import gq.kirmanak.mealient.logging.Logger
import kotlinx.coroutines.suspendCancellableCoroutine
import net.openid.appauth.AuthorizationService
import net.openid.appauth.AuthorizationServiceConfiguration
import net.openid.appauth.TokenRequest
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.coroutines.resume

/**
 * Implementation of OidcTokenExchange using AppAuth library.
 */
@Singleton
class OidcTokenExchangeImpl @Inject constructor(
    private val authService: AuthorizationService,
    private val logger: Logger,
) : OidcTokenExchange {

    override suspend fun exchangeCodeForTokens(
        config: OidcConfig,
        authorizationCode: String,
        codeVerifier: String,
    ): Result<OidcTokens> = suspendCancellableCoroutine { continuation ->
        logger.v { "exchangeCodeForTokens" }

        val serviceConfig = AuthorizationServiceConfiguration(
            Uri.parse(config.authorizationEndpoint),
            Uri.parse(config.tokenEndpoint)
        )

        val tokenRequest = TokenRequest.Builder(
            serviceConfig,
            config.clientId
        ).apply {
            setAuthorizationCode(authorizationCode)
            setRedirectUri(Uri.parse(REDIRECT_URI))
            setCodeVerifier(codeVerifier)
        }.build()

        authService.performTokenRequest(tokenRequest) { response, ex ->
            if (ex != null) {
                logger.e(ex) { "Token exchange failed" }
                continuation.resume(Result.failure(ex))
            } else if (response != null) {
                logger.v { "Token exchange successful" }
                val tokens = OidcTokens(
                    accessToken = response.accessToken.orEmpty(),
                    refreshToken = response.refreshToken,
                    idToken = response.idToken,
                    expiresIn = response.accessTokenExpirationTime
                )
                continuation.resume(Result.success(tokens))
            } else {
                logger.e { "Token exchange returned null response" }
                continuation.resume(Result.failure(Exception("No response from token endpoint")))
            }
        }
    }

    override suspend fun refreshAccessToken(
        config: OidcConfig,
        refreshToken: String,
    ): Result<OidcTokens> = suspendCancellableCoroutine { continuation ->
        logger.v { "refreshAccessToken" }

        val serviceConfig = AuthorizationServiceConfiguration(
            Uri.parse(config.authorizationEndpoint),
            Uri.parse(config.tokenEndpoint)
        )

        val tokenRequest = TokenRequest.Builder(
            serviceConfig,
            config.clientId
        ).apply {
            setRefreshToken(refreshToken)
        }.build()

        authService.performTokenRequest(tokenRequest) { response, ex ->
            if (ex != null) {
                logger.e(ex) { "Token refresh failed" }
                continuation.resume(Result.failure(ex))
            } else if (response != null) {
                logger.v { "Token refresh successful" }
                val tokens = OidcTokens(
                    accessToken = response.accessToken.orEmpty(),
                    refreshToken = response.refreshToken ?: refreshToken, // Keep old refresh token if not renewed
                    idToken = response.idToken,
                    expiresIn = response.accessTokenExpirationTime
                )
                continuation.resume(Result.success(tokens))
            } else {
                logger.e { "Token refresh returned null response" }
                continuation.resume(Result.failure(Exception("No response from token endpoint")))
            }
        }
    }

    companion object {
        private const val REDIRECT_URI = "mealient://oauth/callback"
    }
}
