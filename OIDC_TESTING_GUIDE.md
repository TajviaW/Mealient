# OIDC Testing Guide for Mealient

## Prerequisites

1. **Mealie Server with OIDC**: You need a Mealie server configured with OIDC/SSO
2. **Android Device/Emulator**: To run the Mealient app
3. **Chrome Browser**: For Custom Tabs (usually pre-installed on Android)

## Quick Start Testing

### Step 1: Server Setup

Ensure your Mealie server has OIDC configured. The server should expose:
```
https://your-mealie-server/.well-known/openid-configuration
```

This endpoint should return JSON with at least:
```json
{
  "issuer": "https://your-identity-provider",
  "authorization_endpoint": "https://your-identity-provider/authorize",
  "token_endpoint": "https://your-identity-provider/token",
  "end_session_endpoint": "https://your-identity-provider/logout"
}
```

### Step 2: Build and Install App

```bash
# Build debug APK (requires Java 17+)
./gradlew assembleDebug

# Install on device
adb install app/build/outputs/apk/debug/app-debug.apk
```

### Step 3: Test Basic Flow

1. **Launch App**: Open Mealient
2. **Enter Server URL**: Input your Mealie server URL (e.g., `demo.mealie.io`)
3. **Validate Server**: App validates server and discovers OIDC config
4. **Check OIDC Detection**: Look for "Sign in with SSO" button on login screen
5. **Tap SSO Button**: Chrome Custom Tab should open with OIDC provider login
6. **Authenticate**: Log in with your OIDC provider (e.g., Authelia)
7. **Callback**: Browser redirects back to app
8. **Success**: App should navigate to main screen with authenticated session

## Detailed Test Scenarios

### Scenario 1: OIDC-Only Server

**Expected Behavior:**
- SSO button shown prominently
- "Use password instead" button available
- Password fields hidden by default
- Tapping "Use password instead" reveals password fields

**Test Steps:**
1. Enter server URL
2. Verify SSO button is primary
3. Verify password fields hidden
4. Tap "Use password instead"
5. Verify password fields appear
6. Test SSO login
7. Test password login (should work if server supports both)

### Scenario 2: Password-Only Server

**Expected Behavior:**
- Only password fields shown
- No SSO button visible
- Standard password login works

**Test Steps:**
1. Enter server URL without OIDC
2. Verify no SSO button appears
3. Enter email and password
4. Tap "Login"
5. Verify successful authentication

### Scenario 3: Dual Authentication Server

**Expected Behavior:**
- Password fields shown by default
- "Or sign in with SSO" option below login button
- Both authentication methods work

**Test Steps:**
1. Enter server URL with OIDC
2. Verify password fields shown
3. Verify "Or sign in with SSO" button visible
4. Test password login first
5. Logout
6. Test SSO login
7. Verify both methods store correct auth method

### Scenario 4: Token Refresh

**Expected Behavior:**
- Access token automatically refreshed when expired
- OIDC uses refresh token
- Password uses existing flow
- No user interaction required

**Test Steps:**
1. Log in with SSO
2. Wait for token to expire (check server logs)
3. Make an API call (e.g., view recipes)
4. Verify token refresh happens automatically
5. Verify no error or re-authentication prompt

### Scenario 5: Logout

**Expected Behavior:**
- All tokens cleared from storage
- User redirected to login screen
- Next login requires re-authentication

**Test Steps:**
1. Log in with SSO
2. Navigate to settings
3. Tap "Logout"
4. Verify redirect to login screen
5. Verify tokens cleared (no auto-login)
6. Verify next SSO login prompts for credentials

### Scenario 6: Network Errors

**Expected Behavior:**
- Discovery failure shows password login only
- Token exchange failure shows error message
- User can retry

**Test Steps:**
1. Enter server URL with network issues
2. Verify graceful fallback to password
3. Disconnect network during SSO flow
4. Verify error message shown
5. Reconnect and retry

### Scenario 7: Provider Compatibility

Test with different OIDC providers:

#### Authelia
```bash
# Configuration example
OIDC_ISSUER=https://auth.example.com
CLIENT_ID=mealient-mobile
```

#### Keycloak
```bash
# Configuration example
OIDC_ISSUER=https://keycloak.example.com/realms/mealient
CLIENT_ID=mealient-mobile
```

#### Generic Provider (e.g., Google, Auth0)
```bash
# Works with any OpenID Connect compliant provider
```

**Test Steps:**
1. Configure Mealie with each provider
2. Test discovery works
3. Test authorization flow
4. Test token exchange
5. Test token refresh
6. Test logout

## Debugging Tips

### Enable Debug Logging

Add to logcat filter:
```bash
adb logcat | grep -E "(OidcAuth|OidcToken|AuthKtor)"
```

### Check Discovery Endpoint

```bash
curl https://your-mealie-server/.well-known/openid-configuration
```

### Verify Callback URL

The app is configured to use:
```
mealient://oauth/callback
```

Ensure your OIDC provider accepts this as a valid redirect URI.

### Check Stored Tokens

```bash
# View encrypted SharedPreferences (requires root or debuggable app)
adb shell run-as gq.kirmanak.mealient ls -la /data/data/gq.kirmanak.mealient/shared_prefs/
```

### Common Issues

#### 1. SSO Button Not Appearing
- **Cause**: OIDC discovery failed
- **Solution**: Check server URL, verify `.well-known/openid-configuration` accessible
- **Debug**: Look for "OIDC not available" in logs

#### 2. Redirect Not Working
- **Cause**: Callback URL not registered with provider
- **Solution**: Add `mealient://oauth/callback` to provider's allowed redirects
- **Debug**: Check provider logs for redirect mismatch

#### 3. Token Exchange Fails
- **Cause**: Invalid authorization code or PKCE mismatch
- **Solution**: Verify PKCE enabled on provider, check code_verifier sent
- **Debug**: Enable verbose logging in AppAuth

#### 4. Token Refresh Fails
- **Cause**: Refresh token expired or invalid
- **Solution**: Check refresh token lifetime on provider
- **Debug**: Look for "Token refresh failed" in logs

#### 5. Chrome Custom Tab Not Opening
- **Cause**: No compatible browser installed
- **Solution**: Install Chrome or Samsung Browser
- **Debug**: Check OkHttpClient configuration

## Manual Verification Checklist

### Before Release
- [ ] Test on real Android device (not just emulator)
- [ ] Test with production OIDC provider
- [ ] Test airplane mode / offline behavior
- [ ] Test token expiration and refresh
- [ ] Test logout and re-login
- [ ] Test switching between password and SSO
- [ ] Test with different Android versions
- [ ] Test with different OIDC providers
- [ ] Verify no tokens in plaintext logs
- [ ] Verify tokens stored in encrypted storage
- [ ] Test deep link handling
- [ ] Test app backgrounding during auth flow

### Security Verification
- [ ] PKCE code_challenge sent in auth request
- [ ] State parameter validated on callback
- [ ] Tokens stored in EncryptedSharedPreferences
- [ ] No client secret in code
- [ ] HTTPS enforced for all OIDC endpoints
- [ ] Authorization URL shown in browser (not WebView)
- [ ] No sensitive data in intent extras
- [ ] Token refresh uses refresh token, not credentials

## Sample Test Report Template

```markdown
### Test Report: OIDC Authentication

**Date**: YYYY-MM-DD
**Tester**: Your Name
**App Version**: X.Y.Z
**Server Version**: Mealie vX.Y.Z
**OIDC Provider**: Authelia/Keycloak/Other

#### Environment
- Android Version: 13
- Device: Pixel 6
- Server URL: https://demo.mealie.io

#### Test Results

| Test Case | Status | Notes |
|-----------|--------|-------|
| OIDC Discovery | ✅ Pass | Config detected successfully |
| SSO Login | ✅ Pass | Redirected to Chrome, logged in |
| Callback Handling | ✅ Pass | Returned to app, tokens stored |
| Token Refresh | ✅ Pass | Auto-refreshed after 30 min |
| Logout | ✅ Pass | Tokens cleared, returned to login |
| Password Fallback | ✅ Pass | Can still use password |
| Dual Auth | ✅ Pass | Both methods work |

#### Issues Found
None

#### Recommendations
Consider adding token expiration display in settings.
```

## Developer Testing

### Local Development

```bash
# Run debug build
./gradlew installDebug

# Launch app
adb shell am start -n gq.kirmanak.mealient/.ui.activity.MainActivity

# Watch logs
adb logcat -s "Mealient:*" "OidcAuth:*" "AppAuth:*"
```

### Integration Testing

```kotlin
// Example: Test OIDC discovery
@Test
fun testOidcDiscovery() {
    val mockServer = MockWebServer()
    mockServer.enqueue(MockResponse().setBody("""
        {
            "issuer": "https://auth.example.com",
            "authorization_endpoint": "https://auth.example.com/authorize",
            "token_endpoint": "https://auth.example.com/token"
        }
    """))

    // Test discovery
    val result = oidcDiscoveryDataSource.discoverConfiguration(mockServer.url("/").toString())

    assertTrue(result.issuer == "https://auth.example.com")
}
```

## Production Monitoring

### Metrics to Track
- OIDC discovery success rate
- SSO login success rate
- Token refresh success rate
- Average time to complete SSO flow
- Error rates by error type

### Logging to Watch For
- "OIDC not available" - Discovery failures
- "Token exchange failed" - Authorization issues
- "Token refresh failed" - Refresh token issues
- "State mismatch" - Potential CSRF attacks
- "Invalid redirect" - Configuration issues

---

**Last Updated**: 2026-02-14
**Maintained By**: Development Team
