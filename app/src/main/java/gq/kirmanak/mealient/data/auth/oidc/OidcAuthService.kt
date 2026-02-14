package gq.kirmanak.mealient.data.auth.oidc

import android.content.Context
import android.content.Intent
import android.net.Uri
import dagger.hilt.android.qualifiers.ApplicationContext
import net.openid.appauth.AppAuthConfiguration
import net.openid.appauth.AuthorizationRequest
import net.openid.appauth.AuthorizationService
import net.openid.appauth.AuthorizationServiceConfiguration
import net.openid.appauth.ResponseTypeValues
import net.openid.appauth.browser.BrowserAllowList
import net.openid.appauth.browser.VersionedBrowserMatcher
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Service for launching OIDC authorization flows using Chrome Custom Tabs.
 */
@Singleton
class OidcAuthService @Inject constructor(
    @ApplicationContext private val context: Context,
) {
    private val authService by lazy {
        val browserMatcher = BrowserAllowList(
            VersionedBrowserMatcher.CHROME_CUSTOM_TAB,
            VersionedBrowserMatcher.SAMSUNG_CUSTOM_TAB
        )
        val appAuthConfig = AppAuthConfiguration.Builder()
            .setBrowserMatcher(browserMatcher)
            .build()
        AuthorizationService(context, appAuthConfig)
    }

    /**
     * Creates an intent that launches Chrome Custom Tabs for OIDC authorization.
     *
     * @param authRequest The authorization request to launch
     * @return Intent that can be launched to start the authorization flow
     */
    fun createAuthorizationIntent(authRequest: AuthorizationRequest): Intent {
        return authService.getAuthorizationRequestIntent(authRequest)
    }

    /**
     * Gets the AuthorizationService instance for token operations.
     */
    fun getAuthorizationService(): AuthorizationService = authService

    /**
     * Disposes the authorization service. Should be called when no longer needed.
     */
    fun dispose() {
        authService.dispose()
    }
}
