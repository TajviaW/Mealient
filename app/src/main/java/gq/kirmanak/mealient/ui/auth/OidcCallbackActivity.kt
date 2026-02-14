package gq.kirmanak.mealient.ui.auth

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.lifecycle.lifecycleScope
import dagger.hilt.android.AndroidEntryPoint
import gq.kirmanak.mealient.data.auth.oidc.OidcAuthRepo
import gq.kirmanak.mealient.data.auth.oidc.OidcTokenExchange
import gq.kirmanak.mealient.data.auth.oidc.OidcAuthState
import gq.kirmanak.mealient.data.auth.impl.AuthStorageImpl
import gq.kirmanak.mealient.logging.Logger
import kotlinx.coroutines.launch
import net.openid.appauth.AuthorizationException
import net.openid.appauth.AuthorizationResponse
import javax.inject.Inject

/**
 * Activity that handles the OAuth callback redirect from the browser.
 * This activity receives the authorization code and exchanges it for tokens.
 */
@AndroidEntryPoint
class OidcCallbackActivity : ComponentActivity() {

    @Inject
    lateinit var oidcAuthRepo: OidcAuthRepo

    @Inject
    lateinit var oidcTokenExchange: OidcTokenExchange

    @Inject
    lateinit var authStorage: AuthStorageImpl

    @Inject
    lateinit var logger: Logger

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        logger.v { "OidcCallbackActivity onCreate" }

        handleAuthorizationResponse(intent)
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        logger.v { "OidcCallbackActivity onNewIntent" }
        handleAuthorizationResponse(intent)
    }

    private fun handleAuthorizationResponse(intent: Intent) {
        val response = AuthorizationResponse.fromIntent(intent)
        val exception = AuthorizationException.fromIntent(intent)

        when {
            exception != null -> {
                logger.e(exception) { "OAuth authorization failed" }
                handleError(exception)
            }
            response != null -> {
                logger.v { "OAuth authorization successful, exchanging code for tokens" }
                handleSuccess(response)
            }
            else -> {
                logger.w { "No response or exception in OAuth callback" }
                finishWithResult(Activity.RESULT_CANCELED)
            }
        }
    }

    private fun handleSuccess(response: AuthorizationResponse) {
        lifecycleScope.launch {
            try {
                // Validate the response
                val authorizationCode = response.authorizationCode
                val state = response.state
                val codeVerifier = response.request.codeVerifier

                if (authorizationCode == null || state == null || codeVerifier == null) {
                    logger.e { "Missing required parameters in authorization response" }
                    finishWithResult(Activity.RESULT_CANCELED)
                    return@launch
                }

                // Validate state parameter
                oidcAuthRepo.handleAuthorizationResponse(
                    authorizationCode = authorizationCode,
                    state = state,
                    codeVerifier = codeVerifier
                ).onFailure { error ->
                    logger.e(error) { "Authorization response validation failed" }
                    finishWithResult(Activity.RESULT_CANCELED)
                    return@launch
                }

                // Get OIDC config
                val oidcState = oidcAuthRepo.oidcAuthState.value
                if (oidcState !is OidcAuthState.Configured) {
                    logger.e { "OIDC not configured" }
                    finishWithResult(Activity.RESULT_CANCELED)
                    return@launch
                }

                // Exchange authorization code for tokens
                oidcTokenExchange.exchangeCodeForTokens(
                    config = oidcState.config,
                    authorizationCode = authorizationCode,
                    codeVerifier = codeVerifier
                ).onSuccess { tokens ->
                    logger.v { "Token exchange successful, storing tokens" }
                    // Store tokens
                    authStorage.setOidcTokens(
                        accessToken = tokens.accessToken,
                        refreshToken = tokens.refreshToken,
                        idToken = tokens.idToken
                    )
                    authStorage.setAuthToken(tokens.accessToken)
                    finishWithResult(Activity.RESULT_OK)
                }.onFailure { error ->
                    logger.e(error) { "Token exchange failed" }
                    finishWithResult(Activity.RESULT_CANCELED)
                }
            } catch (e: Exception) {
                logger.e(e) { "Unexpected error handling OAuth callback" }
                finishWithResult(Activity.RESULT_CANCELED)
            }
        }
    }

    private fun handleError(exception: AuthorizationException) {
        logger.e { "OAuth error: ${exception.error} - ${exception.errorDescription}" }
        finishWithResult(Activity.RESULT_CANCELED)
    }

    private fun finishWithResult(resultCode: Int) {
        setResult(resultCode)
        finish()
    }
}
