package gq.kirmanak.mealient.ui.auth

internal data class AuthenticationScreenState(
    val isLoading: Boolean = false,
    val isSuccessful: Boolean = false,
    val errorText: String? = null,
    val emailInput: String = "",
    val passwordInput: String = "",
    val buttonEnabled: Boolean = false,
    val isPasswordVisible: Boolean = false,
    val oidcAvailable: Boolean = false,
    val showPasswordFields: Boolean = true,
    val webBasedOidc: Boolean = false,
    val baseUrl: String? = null,
)