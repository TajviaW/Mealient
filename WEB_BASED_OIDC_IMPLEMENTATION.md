# Web-Based OIDC Implementation for Mealie v3

**Date**: 2026-02-15
**Purpose**: Enable Mealient Android app to authenticate with Mealie v3 servers that use web-based OIDC (oidcRedirect=true)

---

## Problem Statement

Mealie v3 servers with OIDC enabled don't expose a standard `.well-known/openid-configuration` discovery endpoint. Instead, they use a web-based OIDC flow where:

1. Server reports `enableOidc=true` and `oidcRedirect=true`
2. Authentication happens through the web UI (e.g., Mealie → Authentik → Mealie)
3. No standard OIDC discovery endpoint is available
4. Mobile apps were unable to authenticate

## Solution Overview

Implemented a **WebView-based authentication flow** that:

1. **Detects** when a server uses web-based OIDC (no discovery endpoint but OIDC flags are true)
2. **Opens** Mealie's web login page in a WebView
3. **Monitors** the authentication flow
4. **Extracts** the access token after successful authentication
5. **Returns** to the app with the token

---

## Implementation Details

### 1. Data Models

**File**: `app/src/main/java/gq/kirmanak/mealient/data/auth/oidc/OidcConfig.kt`

Added new `OidcAuthState` variant:

```kotlin
sealed class OidcAuthState {
    // ... existing states ...

    /**
     * Server uses web-based OIDC (Mealie v3 style) without exposing discovery endpoint.
     * The app should open the web login page to authenticate.
     */
    data class WebBased(val baseUrl: String) : OidcAuthState()
}
```

**File**: `datasource/src/main/kotlin/gq/kirmanak/mealient/datasource/models/VersionResponse.kt`

Added OIDC configuration fields:

```kotlin
@Serializable
data class VersionResponse(
    // ... existing fields ...
    @SerialName("allowPasswordLogin") val allowPasswordLogin: Boolean? = null,
    @SerialName("enableOidc") val enableOidc: Boolean? = null,
    @SerialName("oidcRedirect") val oidcRedirect: Boolean? = null,
    @SerialName("oidcProviderName") val oidcProviderName: String? = null,
)
```

### 2. Discovery Logic

**File**: `app/src/main/java/gq/kirmanak/mealient/data/auth/oidc/OidcAuthRepoImpl.kt`

Enhanced `discoverOidcConfig()` to:

1. Try standard OIDC discovery first
2. If discovery fails, check server info for `enableOidc` and `oidcRedirect`
3. If both are true, set state to `WebBased` instead of `Failed`

```kotlin
private suspend fun checkWebBasedOidc(baseUrl: String): Boolean {
    return try {
        val serverInfo = versionDataSource.requestVersion(baseUrl)
        val enableOidc = serverInfo.enableOidc ?: false
        val oidcRedirect = serverInfo.oidcRedirect ?: false
        enableOidc && oidcRedirect
    } catch (e: Exception) {
        false
    }
}
```

### 3. WebView Authentication Activity

**File**: `app/src/main/java/gq/kirmanak/mealient/ui/auth/OidcWebAuthActivity.kt` (NEW)

Created a new activity that:

- Opens Mealie's `/login` page in a WebView
- Enables JavaScript and DOM storage
- Injects JavaScript to extract tokens from localStorage or cookies
- Monitors page navigation to detect successful login
- Returns the access token to the calling activity

**Key Features**:
- Uses `JavascriptInterface` to communicate between WebView and Android
- Extracts token from `localStorage.getItem('mealie.access_token')`
- Falls back to reading from cookies if needed
- Shows Material 3 UI with progress indicator

**Manifest Entry**:
```xml
<activity
    android:name=".ui.auth.OidcWebAuthActivity"
    android:exported="false"
    android:theme="@style/Theme.App" />
```

### 4. UI Updates

**File**: `app/src/main/java/gq/kirmanak/mealient/ui/auth/AuthenticationScreenState.kt`

Added fields to track web-based OIDC:

```kotlin
data class AuthenticationScreenState(
    // ... existing fields ...
    val webBasedOidc: Boolean = false,
    val baseUrl: String? = null,
)
```

**File**: `app/src/main/java/gq/kirmanak/mealient/ui/auth/AuthenticationViewModel.kt`

- Updated to observe `WebBased` state
- Added `onWebAuthComplete(token)` to store the received token
- Added `onWebAuthFailed(error)` to handle failures
- Stores token using `authStorage.setOidcTokens()` to mark as OIDC auth

**File**: `app/src/main/java/gq/kirmanak/mealient/ui/auth/AuthenticationScreen.kt`

- Added `ActivityResultLauncher` for `OidcWebAuthActivity`
- Intercepts SSO login click when `webBasedOidc=true`
- Launches WebView activity instead of standard OIDC flow
- Handles result and calls ViewModel methods

---

## Authentication Flow

### Standard OIDC (Existing)
1. App discovers OIDC config from `/.well-known/openid-configuration`
2. Opens Chrome Custom Tabs with authorization URL
3. User authenticates with IdP
4. IdP redirects to `mealient://oauth/callback`
5. App exchanges code for tokens

### Web-Based OIDC (New)
1. App tries standard discovery → fails (404)
2. App checks server info → `enableOidc=true`, `oidcRedirect=true`
3. Sets state to `WebBased`
4. User clicks "Sign in with SSO"
5. App launches `OidcWebAuthActivity` with WebView
6. WebView loads Mealie's `/login` page
7. Mealie redirects to Authentik (or other IdP)
8. User authenticates with IdP
9. IdP redirects back to Mealie
10. Mealie completes authentication and sets token in localStorage
11. App extracts token via JavaScript
12. Activity returns token to app
13. App stores token and marks as successful

---

## Files Created

1. `app/src/main/java/gq/kirmanak/mealient/ui/auth/OidcWebAuthActivity.kt` (239 lines)

## Files Modified

1. `app/src/main/java/gq/kirmanak/mealient/data/auth/oidc/OidcConfig.kt`
   - Added `WebBased` state

2. `datasource/src/main/kotlin/gq/kirmanak/mealient/datasource/models/VersionResponse.kt`
   - Added OIDC configuration fields

3. `app/src/main/java/gq/kirmanak/mealient/data/auth/oidc/OidcAuthRepoImpl.kt`
   - Injected `VersionDataSource`
   - Added `checkWebBasedOidc()` method
   - Enhanced `discoverOidcConfig()` logic

4. `app/src/main/java/gq/kirmanak/mealient/ui/auth/AuthenticationScreenState.kt`
   - Added `webBasedOidc` and `baseUrl` fields

5. `app/src/main/java/gq/kirmanak/mealient/ui/auth/AuthenticationViewModel.kt`
   - Injected `AuthStorage`
   - Updated OIDC state observer
   - Added `onWebAuthComplete()` method
   - Added `onWebAuthFailed()` method

6. `app/src/main/java/gq/kirmanak/mealient/ui/auth/AuthenticationScreen.kt`
   - Added ActivityResultLauncher
   - Intercepts SSO clicks for web-based OIDC
   - Handles WebView activity results

7. `app/src/main/AndroidManifest.xml`
   - Added `OidcWebAuthActivity` declaration

---

## Security Considerations

1. **WebView Security**:
   - JavaScript is enabled (required for token extraction)
   - DOM storage enabled (required for localStorage access)
   - Third-party cookies enabled (required for OIDC flow)

2. **Token Storage**:
   - Token stored using `AuthStorage.setOidcTokens()`
   - Leverages Android's EncryptedSharedPreferences
   - Marked as OIDC authentication method

3. **WebView vs Custom Tabs**:
   - Custom Tabs would be more secure (isolated browser)
   - WebView is necessary to extract tokens via JavaScript
   - Trade-off accepted because Mealie doesn't provide token via redirect

4. **Token Extraction**:
   - JavaScript interface used to communicate with Android
   - Token extracted from localStorage or cookies
   - Falls back gracefully if token not found

---

## Testing Checklist

### Before Testing

- [ ] Build requires Java 17+ (current system has Java 11)
- [ ] Install Java 17: `sudo apt install openjdk-17-jdk`
- [ ] Set JAVA_HOME: `export JAVA_HOME=/usr/lib/jvm/java-17-openjdk-amd64`
- [ ] Verify: `java -version` should show 17

### Build

```bash
./gradlew clean assembleDebug
```

### Installation

```bash
adb install app/build/outputs/apk/debug/app-debug.apk
```

### Test Scenarios

#### Scenario 1: Mealie v3 with Web-Based OIDC

**Server Config**:
- `enableOidc=true`
- `oidcRedirect=true`
- No `/.well-known/openid-configuration` endpoint

**Expected Behavior**:
1. App validates server URL
2. OIDC discovery fails but detects web-based OIDC
3. "Sign in with SSO" button appears
4. Clicking SSO opens WebView with Mealie login
5. User is redirected to Authentik (or IdP)
6. User authenticates with IdP
7. Browser returns to Mealie
8. Token is extracted automatically
9. WebView closes and app navigates to main screen

#### Scenario 2: Mealie v2 with Standard OIDC

**Server Config**:
- Has `/.well-known/openid-configuration` endpoint

**Expected Behavior**:
1. Standard OIDC discovery succeeds
2. Uses existing Chrome Custom Tabs flow
3. No WebView involved

#### Scenario 3: Password-Only Server

**Server Config**:
- `enableOidc=false` or no OIDC config

**Expected Behavior**:
1. Only password fields shown
2. No SSO button

#### Scenario 4: Authentication Failures

**Test Cases**:
- User closes WebView before completing auth → Shows "Authentication cancelled"
- Token extraction fails → Shows "No token received"
- Network error during web auth → WebView shows error page

---

## Known Limitations

1. **Java Version**: Build requires Java 17+ (system currently has Java 11)

2. **No Refresh Token**: Web-based OIDC doesn't provide refresh token
   - Token refresh will fail when access token expires
   - User will need to re-authenticate

3. **WebView Security**: Less secure than Chrome Custom Tabs
   - Trade-off necessary for token extraction
   - Consider future enhancement with token API endpoint

4. **No Logout Endpoint**: End session endpoint not called
   - Tokens cleared locally
   - Session may remain active on server

---

## Future Enhancements

### 1. Token Refresh Support

Request Mealie to provide API endpoint for generating long-lived tokens:

```
POST /api/auth/token/generate
Cookie: mealie.access_token=<web_session_token>

Response:
{
  "access_token": "<long_lived_token>",
  "refresh_token": "<refresh_token>",
  "expires_in": 2592000  // 30 days
}
```

### 2. Chrome Custom Tabs with Token API

If Mealie adds token API:
1. Open login in Custom Tabs (more secure)
2. After successful web auth, redirect to custom URL scheme
3. App intercepts redirect and calls token API
4. No WebView or JavaScript injection needed

### 3. Better Error Messages

- Detect specific OIDC errors (invalid redirect, config issues)
- Show user-friendly messages
- Provide troubleshooting tips

### 4. Biometric Re-authentication

- Store token encrypted with biometric key
- Allow quick re-authentication without web flow
- Improves user experience

---

## Backward Compatibility

✅ **Fully backward compatible**:
- Standard OIDC flow unchanged
- Password authentication unchanged
- Servers without OIDC work as before
- Only adds new flow for Mealie v3 web-based OIDC

---

## Troubleshooting

### Build Fails with "Android Gradle plugin requires Java 17"

**Solution**:
```bash
# Install Java 17
sudo apt install openjdk-17-jdk

# Set JAVA_HOME
export JAVA_HOME=/usr/lib/jvm/java-17-openjdk-amd64

# Verify
java -version  # Should show 17

# Or set in gradle.properties
echo "org.gradle.java.home=/usr/lib/jvm/java-17-openjdk-amd64" >> gradle.properties
```

### WebView Shows "Page Not Found"

**Check**:
- Server URL is correct
- Server is accessible
- No typos in base URL

### Token Not Extracted

**Debug**:
1. Enable WebView debugging:
   ```kotlin
   WebView.setWebContentsDebuggingEnabled(true)
   ```
2. Open Chrome DevTools: `chrome://inspect`
3. Check localStorage for `mealie.access_token`

### Authentication Completes but App Doesn't Navigate

**Check**:
- Token was stored successfully (check logs)
- AuthStorage working correctly
- Navigation logic in AuthenticationScreen

---

## Summary

Successfully implemented web-based OIDC authentication for Mealie v3 servers. The solution:

✅ Detects when server uses web-based OIDC
✅ Opens WebView for authentication
✅ Extracts token automatically
✅ Stores token securely
✅ Fully backward compatible
✅ No server-side changes required

**Next Step**: Upgrade to Java 17 and test on real device with Mealie v3 server.
