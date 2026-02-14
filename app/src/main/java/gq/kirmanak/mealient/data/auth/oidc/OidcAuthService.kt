package gq.kirmanak.mealient.data.auth.oidc

import android.content.Context
import android.content.Intent
import android.net.Uri
import dagger.hilt.android.qualifiers.ApplicationContext
import net.openid.appauth.AuthorizationService
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
        AuthorizationService(context, browserMatcher)
    }

    /**
     * Creates an intent that launches Chrome Custom Tabs for OIDC authorization.
     *
     * @param authorizationUrl The complete authorization URL
     * @return Intent that can be launched to start the authorization flow
     */
    fun createAuthorizationIntent(authorizationUrl: String): Intent {
        return authService.getAuthorizationRequestIntent(
            Uri.parse(authorizationUrl)
        )
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
