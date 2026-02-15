package gq.kirmanak.mealient.data.auth.oidc

import android.net.Uri
import gq.kirmanak.mealient.datasource.oidc.OidcDiscoveryDataSource
import gq.kirmanak.mealient.logging.Logger
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import net.openid.appauth.AuthorizationRequest
import net.openid.appauth.AuthorizationServiceConfiguration
import net.openid.appauth.CodeVerifierUtil
import net.openid.appauth.ResponseTypeValues
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Implementation of OidcAuthRepo using AppAuth library.
 */
@Singleton
class OidcAuthRepoImpl @Inject constructor(
    private val oidcDiscoveryDataSource: OidcDiscoveryDataSource,
    private val logger: Logger,
) : OidcAuthRepo {

    private val _oidcAuthState = MutableStateFlow<OidcAuthState>(OidcAuthState.NotConfigured)
    override val oidcAuthState: StateFlow<OidcAuthState> = _oidcAuthState.asStateFlow()

    private var currentAuthorizationRequest: AuthorizationRequest? = null

    override suspend fun discoverOidcConfig(baseUrl: String): Result<OidcConfig> {
        logger.v { "discoverOidcConfig: baseUrl=$baseUrl" }
        _oidcAuthState.value = OidcAuthState.Pending

        return try {
            val enabled = oidcDiscoveryDataSource.checkOidcEnabled(baseUrl)
            if (!enabled) {
                logger.v { "OIDC not enabled on server" }
                _oidcAuthState.value = OidcAuthState.NotConfigured
                return Result.failure(Exception("OIDC not enabled on this server"))
            }

            val discoveryResponse = oidcDiscoveryDataSource.discoverConfiguration(baseUrl)
            val config = OidcConfig(
                authorizationEndpoint = discoveryResponse.authorization_endpoint,
                tokenEndpoint = discoveryResponse.token_endpoint,
                endSessionEndpoint = discoveryResponse.end_session_endpoint,
                issuer = discoveryResponse.issuer,
            )

            _oidcAuthState.value = OidcAuthState.Configured(config)
            logger.v { "OIDC configuration discovered successfully" }
            Result.success(config)
        } catch (e: Exception) {
            logger.e(e) { "Failed to discover OIDC configuration" }
            _oidcAuthState.value = OidcAuthState.Failed(e.message ?: "Unknown error")
            Result.failure(e)
        }
    }

    override suspend fun startOidcFlow(): Result<OidcAuthorizationRequest> {
        logger.v { "startOidcFlow" }
        val state = oidcAuthState.value

        if (state !is OidcAuthState.Configured) {
            logger.w { "Cannot start OIDC flow: not configured" }
            return Result.failure(Exception("OIDC not configured"))
        }

        return try {
            val config = state.config
            val serviceConfig = AuthorizationServiceConfiguration(
                Uri.parse(config.authorizationEndpoint),
                Uri.parse(config.tokenEndpoint)
            )

            // Generate PKCE code verifier
            val codeVerifier = CodeVerifierUtil.generateRandomCodeVerifier()

            // Build authorization request
            val authRequestBuilder = AuthorizationRequest.Builder(
                serviceConfig,
                config.clientId,
                ResponseTypeValues.CODE,
                Uri.parse(REDIRECT_URI)
            ).apply {
                setCodeVerifier(codeVerifier)
                setScopes("openid", "profile", "email")
            }

            val authRequest = authRequestBuilder.build()
            currentAuthorizationRequest = authRequest

            val authorizationUrl = authRequest.toUri().toString()
            val state = authRequest.state ?: throw IllegalStateException("State parameter is null")
            logger.v { "Authorization URL generated" }

            Result.success(
                OidcAuthorizationRequest(
                    authorizationUrl = authorizationUrl,
                    state = state,
                    codeVerifier = codeVerifier,
                    authRequest = authRequest
                )
            )
        } catch (e: Exception) {
            logger.e(e) { "Failed to start OIDC flow" }
            Result.failure(e)
        }
    }

    override suspend fun handleAuthorizationResponse(
        authorizationCode: String,
        state: String,
        codeVerifier: String,
    ): Result<Unit> {
        logger.v { "handleAuthorizationResponse" }

        // Validate state parameter
        val expectedState = currentAuthorizationRequest?.state
        if (expectedState != state) {
            logger.e { "State mismatch: expected=$expectedState, received=$state" }
            return Result.failure(Exception("Invalid state parameter"))
        }

        // The actual token exchange will be handled by OidcTokenExchange
        // This is just validation and state management
        logger.v { "Authorization response validated successfully" }
        return Result.success(Unit)
    }

    companion object {
        private const val REDIRECT_URI = "mealient://oauth/callback"
    }
}
