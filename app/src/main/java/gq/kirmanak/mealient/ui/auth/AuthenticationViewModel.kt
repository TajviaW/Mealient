package gq.kirmanak.mealient.ui.auth

import android.app.Application
import android.content.Intent
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import gq.kirmanak.mealient.R
import gq.kirmanak.mealient.data.auth.AuthRepo
import gq.kirmanak.mealient.data.auth.AuthStorage
import gq.kirmanak.mealient.data.auth.oidc.OidcAuthRepo
import gq.kirmanak.mealient.data.auth.oidc.OidcAuthorizationRequest
import gq.kirmanak.mealient.data.auth.oidc.OidcAuthService
import gq.kirmanak.mealient.data.auth.oidc.OidcAuthState
import gq.kirmanak.mealient.data.baseurl.ServerInfoRepo
import gq.kirmanak.mealient.datasource.NetworkError
import gq.kirmanak.mealient.datasource.runCatchingExceptCancel
import gq.kirmanak.mealient.logging.Logger
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.flow.updateAndGet
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
internal class AuthenticationViewModel @Inject constructor(
    private val application: Application,
    private val authRepo: AuthRepo,
    private val authStorage: AuthStorage,
    private val oidcAuthRepo: OidcAuthRepo,
    private val oidcAuthService: OidcAuthService,
    private val serverInfoRepo: ServerInfoRepo,
    private val logger: Logger,
) : ViewModel() {

    private val _screenState = MutableStateFlow(AuthenticationScreenState())
    val screenState = _screenState.asStateFlow()

    private var discoveryTriggered = false

    init {
        // Observe OIDC availability
        viewModelScope.launch {
            oidcAuthRepo.oidcAuthState.collect { state ->
                logger.d { "OIDC state changed: $state" }

                // Trigger discovery once if needed
                if (!discoveryTriggered && state is OidcAuthState.NotConfigured) {
                    val baseUrl = serverInfoRepo.getUrl()
                    if (baseUrl != null) {
                        discoveryTriggered = true
                        logger.v { "Triggering OIDC discovery for $baseUrl" }
                        launch { oidcAuthRepo.discoverOidcConfig(baseUrl) }
                    }
                }

                _screenState.update {
                    when (state) {
                        is OidcAuthState.Configured -> it.copy(
                            oidcAvailable = true,
                            webBasedOidc = false
                        )
                        is OidcAuthState.WebBased -> it.copy(
                            oidcAvailable = true,
                            webBasedOidc = true,
                            baseUrl = state.baseUrl
                        )
                        else -> it.copy(
                            oidcAvailable = false,
                            webBasedOidc = false,
                            baseUrl = null
                        )
                    }
                }
            }
        }
    }

    fun onEvent(event: AuthenticationScreenEvent) {
        logger.v { "onEvent() called with: event = $event" }
        when (event) {
            is AuthenticationScreenEvent.OnLoginClick -> {
                onLoginClick()
            }

            is AuthenticationScreenEvent.OnEmailInput -> {
                onEmailInput(event.input)
            }

            is AuthenticationScreenEvent.OnPasswordInput -> {
                onPasswordInput(event.input)
            }

            AuthenticationScreenEvent.TogglePasswordVisibility -> {
                togglePasswordVisibility()
            }

            AuthenticationScreenEvent.OnSsoLoginClick -> {
                onSsoLoginClick()
            }

            AuthenticationScreenEvent.OnShowPasswordLogin -> {
                onShowPasswordLogin()
            }
        }
    }

    private fun togglePasswordVisibility() {
        _screenState.update {
            it.copy(isPasswordVisible = !it.isPasswordVisible)
        }
    }

    private fun onPasswordInput(passwordInput: String) {
        _screenState.update {
            it.copy(
                passwordInput = passwordInput,
                buttonEnabled = passwordInput.isNotEmpty() && it.emailInput.isNotEmpty(),
            )
        }
    }

    private fun onEmailInput(emailInput: String) {
        _screenState.update {
            it.copy(
                emailInput = emailInput.trim(),
                buttonEnabled = emailInput.isNotEmpty() && it.passwordInput.isNotEmpty(),
            )
        }
    }

    private fun onLoginClick() {
        val screenState = _screenState.updateAndGet {
            it.copy(
                isLoading = true,
                errorText = null,
                buttonEnabled = false,
            )
        }
        viewModelScope.launch {
            val result = runCatchingExceptCancel {
                authRepo.authenticate(
                    email = screenState.emailInput,
                    password = screenState.passwordInput
                )
            }
            logger.d { "onLoginClick: result = $result" }
            val errorText = result.fold(
                onSuccess = { null },
                onFailure = {
                    when (it) {
                        is NetworkError.Unauthorized -> application.getString(R.string.fragment_authentication_credentials_incorrect)
                        else -> it.message
                    }
                }
            )
            _screenState.update {
                it.copy(
                    isLoading = false,
                    isSuccessful = result.isSuccess,
                    errorText = errorText,
                    buttonEnabled = true,
                )
            }
        }
    }

    private fun onSsoLoginClick() {
        logger.v { "onSsoLoginClick" }
        _screenState.update {
            it.copy(
                isLoading = true,
                errorText = null,
            )
        }
        viewModelScope.launch {
            val result = oidcAuthRepo.startOidcFlow()

            result.fold(
                onSuccess = { oidcAuthRequest ->
                    logger.v { "Starting OIDC flow with authorization URL" }
                    application.startActivity(
                        oidcAuthService.createAuthorizationIntent(oidcAuthRequest).apply {
                            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                        }
                    )

                    _screenState.update {
                        it.copy(isLoading = false)
                    }
                },
                onFailure = { error ->
                    logger.e(error) { "Failed to start OIDC flow" }
                    _screenState.update {
                        it.copy(
                            isLoading = false,
                            errorText = error.message ?: application.getString(R.string.fragment_authentication_sso_error)
                        )
                    }
                }
            )
        }
    }

    private fun onShowPasswordLogin() {
        logger.v { "onShowPasswordLogin" }
        _screenState.update {
            it.copy(showPasswordFields = true)
        }
    }

    /**
     * Called when web-based authentication completes successfully with a token.
     * Stores the token and marks authentication as successful.
     */
    fun onWebAuthComplete(token: String) {
        logger.v { "onWebAuthComplete: token received" }
        viewModelScope.launch {
            try {
                // Store the token as OIDC authentication
                // We don't have refresh/ID tokens from web-based flow
                authStorage.setOidcTokens(
                    accessToken = token,
                    refreshToken = null,
                    idToken = null
                )
                authStorage.setAuthToken(token)

                _screenState.update {
                    it.copy(
                        isLoading = false,
                        isSuccessful = true,
                        errorText = null
                    )
                }
            } catch (e: Exception) {
                logger.e(e) { "Failed to store web auth token" }
                _screenState.update {
                    it.copy(
                        isLoading = false,
                        errorText = e.message ?: "Failed to complete authentication"
                    )
                }
            }
        }
    }

    /**
     * Called when web-based authentication fails or is cancelled.
     */
    fun onWebAuthFailed(error: String? = null) {
        logger.v { "onWebAuthFailed: $error" }
        _screenState.update {
            it.copy(
                isLoading = false,
                errorText = error ?: application.getString(R.string.fragment_authentication_sso_error)
            )
        }
    }

}