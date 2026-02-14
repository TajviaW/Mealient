package gq.kirmanak.mealient.datasource.ktor

import gq.kirmanak.mealient.datasource.TokenChangeListener
import gq.kirmanak.mealient.logging.Logger
import io.ktor.client.HttpClient
import io.ktor.client.plugins.auth.authProvider
import io.ktor.client.plugins.auth.providers.BearerAuthProvider
import javax.inject.Inject

internal class TokenChangeListenerKtor @Inject constructor(
    private val httpClient: HttpClient,
    private val logger: Logger,
) : TokenChangeListener {

    override fun onTokenChange() {
        logger.v { "onTokenChange() called" }
        val provider = httpClient.authProvider<BearerAuthProvider>()
        if (provider != null) {
            logger.d { "onTokenChange(): removing the token" }
            provider.clearToken()
        } else {
            logger.w { "onTokenChange(): BearerAuthProvider not found" }
        }
    }
}