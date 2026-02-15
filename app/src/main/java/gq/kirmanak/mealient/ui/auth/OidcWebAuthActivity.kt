package gq.kirmanak.mealient.ui.auth

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.webkit.CookieManager
import android.webkit.JavascriptInterface
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.viewinterop.AndroidView
import dagger.hilt.android.AndroidEntryPoint
import gq.kirmanak.mealient.logging.Logger
import gq.kirmanak.mealient.ui.theme.MealientTheme
import javax.inject.Inject

/**
 * Activity that opens Mealie's web login page in a WebView for OIDC authentication.
 * After successful login, it extracts the authentication token and returns it to the caller.
 */
@AndroidEntryPoint
class OidcWebAuthActivity : ComponentActivity() {

    @Inject
    lateinit var logger: Logger

    private var webView: WebView? = null
    private var isLoading by mutableStateOf(true)
    private var loadProgress by mutableStateOf(0)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        logger.v { "OidcWebAuthActivity onCreate" }

        val baseUrl = intent.getStringExtra(EXTRA_BASE_URL)
        if (baseUrl == null) {
            logger.e { "No base URL provided" }
            setResult(RESULT_CANCELED)
            finish()
            return
        }

        setContent {
            MealientTheme {
                OidcWebAuthScreen(
                    baseUrl = baseUrl,
                    isLoading = isLoading,
                    loadProgress = loadProgress,
                    onWebViewCreated = { webView ->
                        this.webView = webView
                        setupWebView(webView, baseUrl)
                    }
                )
            }
        }
    }

    @SuppressLint("SetJavaScriptEnabled")
    private fun setupWebView(webView: WebView, baseUrl: String) {
        logger.v { "Setting up WebView for $baseUrl" }

        // Enable JavaScript (required for modern web apps)
        webView.settings.apply {
            javaScriptEnabled = true
            domStorageEnabled = true
            databaseEnabled = true
        }

        // Enable cookies to maintain session
        CookieManager.getInstance().apply {
            setAcceptCookie(true)
            setAcceptThirdPartyCookies(webView, true)
        }

        // Add JavaScript interface to extract token
        webView.addJavascriptInterface(TokenExtractor(), "AndroidInterface")

        webView.webViewClient = object : WebViewClient() {
            override fun onPageStarted(view: WebView?, url: String?, favicon: android.graphics.Bitmap?) {
                super.onPageStarted(view, url, favicon)
                logger.v { "Page loading: $url" }
                isLoading = true
            }

            override fun onPageFinished(view: WebView?, url: String?) {
                super.onPageFinished(view, url)
                logger.v { "Page loaded: $url" }
                isLoading = false

                // After page loads, try to extract token
                if (url?.contains(baseUrl) == true && !url.contains("/login")) {
                    // User is logged in, try to extract the token
                    extractToken(view)
                }
            }

            override fun onLoadResource(view: WebView?, url: String?) {
                super.onLoadResource(view, url)
                // Update progress
                loadProgress = view?.progress ?: 0
            }
        }

        // Load the login page
        val loginUrl = "$baseUrl/login"
        logger.v { "Loading login page: $loginUrl" }
        webView.loadUrl(loginUrl)
    }

    private fun extractToken(webView: WebView?) {
        logger.v { "Attempting to extract authentication token" }

        // Try to get the token from localStorage or cookies
        val script = """
            (function() {
                try {
                    // Try to get token from localStorage
                    var token = localStorage.getItem('mealie.access_token');
                    if (token) {
                        AndroidInterface.onTokenFound(token);
                        return;
                    }

                    // Try to get token from cookies
                    var cookies = document.cookie.split(';');
                    for (var i = 0; i < cookies.length; i++) {
                        var cookie = cookies[i].trim();
                        if (cookie.startsWith('mealie.access_token=')) {
                            var tokenValue = cookie.substring('mealie.access_token='.length);
                            AndroidInterface.onTokenFound(tokenValue);
                            return;
                        }
                    }

                    // If we're on the main page but no token found, try again in a moment
                    setTimeout(function() {
                        token = localStorage.getItem('mealie.access_token');
                        if (token) {
                            AndroidInterface.onTokenFound(token);
                        } else {
                            AndroidInterface.onTokenNotFound();
                        }
                    }, 1000);
                } catch (e) {
                    AndroidInterface.onError('Error extracting token: ' + e.message);
                }
            })();
        """.trimIndent()

        webView?.evaluateJavascript(script, null)
    }

    inner class TokenExtractor {
        @JavascriptInterface
        fun onTokenFound(token: String) {
            logger.v { "Token found via JavaScript interface" }
            runOnUiThread {
                val resultIntent = Intent().apply {
                    putExtra(EXTRA_TOKEN, token)
                }
                setResult(RESULT_OK, resultIntent)
                finish()
            }
        }

        @JavascriptInterface
        fun onTokenNotFound() {
            logger.w { "Token not found in localStorage or cookies" }
            // Continue showing WebView - user might still be authenticating
        }

        @JavascriptInterface
        fun onError(error: String) {
            logger.e { "JavaScript error: $error" }
            runOnUiThread {
                setResult(RESULT_CANCELED)
                finish()
            }
        }
    }

    override fun onBackPressed() {
        if (webView?.canGoBack() == true) {
            webView?.goBack()
        } else {
            super.onBackPressed()
            setResult(RESULT_CANCELED)
        }
    }

    override fun onDestroy() {
        webView?.destroy()
        webView = null
        super.onDestroy()
    }

    companion object {
        private const val EXTRA_BASE_URL = "extra_base_url"
        private const val EXTRA_TOKEN = "extra_token"

        fun createIntent(context: Context, baseUrl: String): Intent {
            return Intent(context, OidcWebAuthActivity::class.java).apply {
                putExtra(EXTRA_BASE_URL, baseUrl)
            }
        }

        fun getTokenFromResult(data: Intent?): String? {
            return data?.getStringExtra(EXTRA_TOKEN)
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun OidcWebAuthScreen(
    baseUrl: String,
    isLoading: Boolean,
    loadProgress: Int,
    onWebViewCreated: (WebView) -> Unit
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Sign in to Mealie") }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            // Show loading progress
            if (isLoading && loadProgress > 0) {
                LinearProgressIndicator(
                    progress = loadProgress / 100f,
                    modifier = Modifier.fillMaxWidth()
                )
            }

            // WebView
            AndroidView(
                factory = { context ->
                    WebView(context).also { webView ->
                        onWebViewCreated(webView)
                    }
                },
                modifier = Modifier.fillMaxSize()
            )
        }
    }
}
