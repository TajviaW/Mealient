package gq.kirmanak.mealient.data.auth.impl

import gq.kirmanak.mealient.data.auth.AuthDataSource
import gq.kirmanak.mealient.data.auth.AuthMethod
import gq.kirmanak.mealient.data.auth.AuthRepo
import gq.kirmanak.mealient.data.auth.AuthStorage
import gq.kirmanak.mealient.data.auth.oidc.OidcTokenRefresh
import gq.kirmanak.mealient.datasource.AuthenticationProvider
import gq.kirmanak.mealient.logging.Logger
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class AuthRepoImpl @Inject constructor(
    private val authStorage: AuthStorage,
    private val authDataSource: AuthDataSource,
    private val oidcTokenRefresh: OidcTokenRefresh,
    private val logger: Logger,
    private val credentialsLogRedactor: CredentialsLogRedactor,
) : AuthRepo, AuthenticationProvider {

    override val isAuthorizedFlow: Flow<Boolean>
        get() = authStorage.authTokenFlow.map { it != null }

    override suspend fun authenticate(email: String, password: String) {
        logger.v { "authenticate() called" }

        credentialsLogRedactor.set(email, password)
        val token = authDataSource.authenticate(email, password)
        credentialsLogRedactor.clear()
        authStorage.setAuthToken(token)

        val apiToken = authDataSource.createApiToken(API_TOKEN_NAME)
        authStorage.setPasswordAuthToken(apiToken)
    }

    override suspend fun getAuthToken(): String? = authStorage.getAuthToken()

    override suspend fun getAuthMethod(): gq.kirmanak.mealient.datasource.AuthMethod {
        val method = authStorage.authMethodFlow.first()
        return when (method) {
            AuthMethod.PASSWORD -> gq.kirmanak.mealient.datasource.AuthMethod.PASSWORD
            AuthMethod.OIDC -> gq.kirmanak.mealient.datasource.AuthMethod.OIDC
            AuthMethod.NONE -> gq.kirmanak.mealient.datasource.AuthMethod.NONE
        }
    }

    override suspend fun refreshOidcToken(): String? {
        logger.v { "refreshOidcToken() called" }
        val tokens = oidcTokenRefresh.refreshIfNeeded()
        return tokens?.accessToken
    }

    override suspend fun logout() {
        logger.v { "logout() called" }
        authStorage.clearOidcTokens()
        authStorage.setAuthToken(null)
    }

    companion object {
        private const val API_TOKEN_NAME = "Mealient"
    }
}