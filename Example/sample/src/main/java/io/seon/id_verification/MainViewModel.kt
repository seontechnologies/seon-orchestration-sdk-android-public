package io.seon.orchestration_sample

import androidx.lifecycle.ViewModel
import io.seon.orch_sdk.pub.OrchestrationService
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class MainViewModel(
    private val orchestrationService: OrchestrationService
) : ViewModel() {

    // UI State
    private val _uiState = MutableStateFlow(MainUiState())
    val uiState: StateFlow<MainUiState> = _uiState.asStateFlow()

    // Events
    private val _events = MutableStateFlow<MainEvent?>(null)
    val events: StateFlow<MainEvent?> = _events.asStateFlow()

    fun onStartVerificationClicked(
        language: String?,
        theme: String?,
        sessionToken: String?,
    ) {
        if (_uiState.value.isLoading) return

        _uiState.value = _uiState.value.copy(isLoading = true)
        handleSessionTokenMode(language, theme, sessionToken)
    }

    private fun handleSessionTokenMode(language: String?, theme: String?, sessionToken: String?) {
        if (sessionToken.isNullOrEmpty()) {
            _uiState.value = _uiState.value.copy(isLoading = false)
            _events.value = MainEvent.ShowError("Please enter a valid JWT session token.")
            return
        }

        // Initialize SDK with provided session token
        initializeSDKWithToken(sessionToken, language, theme)
    }
    private fun initializeSDKWithToken(token: String, language: String?, theme: String?) {
        // The SDK now handles token parsing, region fetching, and client-init internally
        // you may specify your baseUrl based on the region:
        // Environment    URL
        // EU             https://api.seon.io/orchestration-api
        // US             https://api.us-east-1-main.seon.io/orchestration-api
        // APAC           https://api.ap-southeast-1-main.seon.io/orchestration-api
        orchestrationService.initialize(
            baseUrl = "https://api.seon.io",
            token = token,
            languageCode = language?.lowercase(),
            theme = theme
        )

        _events.value = MainEvent.StartVerificationFlow
        _uiState.value = _uiState.value.copy(isLoading = false)
    }

    fun onEventConsumed() {
        _events.value = null
    }
}

// UI State
data class MainUiState(
    val isLoading: Boolean = false
)

// Events
sealed class MainEvent {
    data class ShowMessage(val message: String) : MainEvent()
    data class ShowError(val message: String) : MainEvent()
    object StartVerificationFlow : MainEvent()
}
