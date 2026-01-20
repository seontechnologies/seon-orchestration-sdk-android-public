package io.seon.orchestration_sample

import android.content.SharedPreferences
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.launch
import androidx.activity.result.ActivityResult
import androidx.activity.result.contract.ActivityResultContracts.StartActivityForResult
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.platform.testTag
import io.seon.orch_sdk.pub.Constants.VERIFICATION_ERROR_KEY
import io.seon.orch_sdk.pub.OrchestrationService
import io.seon.orch_sdk.pub.SEONOrchFlowResult
import androidx.compose.ui.unit.sp
import io.seon.id_verification.ui.theme.SEONOrchTheme

class MainActivity : ComponentActivity() {

    private val verificationResultText = mutableStateOf("")
    private val verificationResultTextColor = mutableStateOf(Color.Unspecified)
    private val verificationActivityResultLauncher =
        registerForActivityResult(StartActivityForResult()) { result: ActivityResult ->
            handleVerificationResult(result)
        }
    private val orchestrationService = OrchestrationService.instance
    private var isNavigating = false

    // ViewModel
    private val viewModel: MainViewModel by viewModels {
        MainViewModelFactory(orchestrationService)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Observe ViewModel events
        lifecycleScope.launch {
            viewModel.events.collect { event ->
                event?.let { handleEvent(it) }
            }
        }

        setContent {
            SampleContent(
                buttonClick = { language, theme, sessionToken ->
                    if (!isNavigating) {
                        viewModel.onStartVerificationClicked(
                            language = language,
                            theme = theme,
                            sessionToken = sessionToken
                        )

                        isNavigating = true
                        Handler(Looper.getMainLooper()).postDelayed({
                            isNavigating = false
                        }, 500L)
                    }
                }
            )
        }
    }

    private fun handleEvent(event: MainEvent) {
        when (event) {
            is MainEvent.ShowMessage -> {
                android.widget.Toast.makeText(
                    this,
                    event.message,
                    android.widget.Toast.LENGTH_LONG
                ).show()
            }
            is MainEvent.ShowError -> {
                android.widget.Toast.makeText(
                    this,
                    event.message,
                    android.widget.Toast.LENGTH_LONG
                ).show()
            }
            is MainEvent.StartVerificationFlow -> {
                println("🚀 MainActivity: Received StartVerificationFlow event")
                println("🔧 MainActivity: Calling orchestrationService.startVerificationFlow")
                orchestrationService.startVerificationFlow(
                    verificationActivityResultLauncher,
                    applicationContext
                )
                println("✅ MainActivity: startWebVerificationFlow call completed")
            }
        }
        viewModel.onEventConsumed()
    }

    private fun handleVerificationResult(result: ActivityResult) {
        when (result.resultCode) {

            SEONOrchFlowResult.InterruptedByUser.code -> {
                verificationResultTextColor.value = Color(red = 220, green = 120, blue = 0)
                verificationResultText.value =
                    SEONOrchFlowResult.InterruptedByUser.name
            }

            SEONOrchFlowResult.Error.code -> {
                val error = result.data?.getStringExtra(VERIFICATION_ERROR_KEY)
                verificationResultTextColor.value = Color(red = 178, green = 34, blue = 34)
                val trimmedError : String = when (error?.isNotEmpty()) {
                    true -> error
                    else -> SEONOrchFlowResult.Error.name
                }
                verificationResultText.value = trimmedError
            }

            SEONOrchFlowResult.Completed.code -> {
                verificationResultTextColor.value = Color(red = 34, green = 139, blue = 34)
                verificationResultText.value =
                    SEONOrchFlowResult.Completed.name
            }

            SEONOrchFlowResult.CompletedSuccess.code -> {
                verificationResultTextColor.value = Color(red = 34, green = 139, blue = 34)
                verificationResultText.value =
                    SEONOrchFlowResult.CompletedSuccess.name
            }

            SEONOrchFlowResult.CompletedPending.code -> {
                verificationResultTextColor.value = Color(red = 34, green = 139, blue = 34)
                verificationResultText.value =
                    SEONOrchFlowResult.CompletedPending.name
            }

            SEONOrchFlowResult.CompletedFailed.code -> {
                verificationResultTextColor.value = Color(red = 178, green = 34, blue = 34)
                verificationResultText.value =
                    SEONOrchFlowResult.CompletedFailed.name
            }
        }
    }

    @OptIn(ExperimentalMaterial3Api::class)
    @Composable
    fun SampleContent(
        buttonClick: (
            language: String?,
            theme: String?,
            sessionToken: String?
        ) -> Unit
    ) {
        val defaultTheme = """{
            "light":{
                "baseTextOnLight":"#000000",
                "baseTextOnDark":"#FFFFFF",
                "baseAccent":"#0063FF",
                "baseOnAccent":"#FFFFFF"
            },
            "dark":{
                "baseTextOnLight":"#FFFFFF",
                "baseTextOnDark":"#000000",
                "baseAccent":"#4185F2",
                "baseOnAccent":"#000000"
            },
            "fontFamily":"idverif-default",
            "fontUrl":"./fonts/Inter-VariableFont_slnt.ttf",
            "fontWeight":"400"
        }
        """.trimMargin()
        val defaultWorkflowParams = """{
          "ip": "213.253.227.38",
          "email": "abbas.sabetinezhad@seon.io",
          "user_id": "abbas-2026-01-07-random330023647788e",
          "user_dob": "1992-11-28",
          "user_pob": "Dunaujvaros",
          "user_zip": "15366",
          "device_id": "A68JCP3C21403957",
          "phone_number": "491713647162",
          "user_country": "DE",
          "user_fullname": "Abbas Sabetinezhad",
          "reference_image": "e04d4b35-f038-4cd5-aeb3-1519ac674bb6"
        }
        """.trimMargin()

        val language = remember { mutableStateOf("en") }
        val customizeTheme = remember { mutableStateOf(false) }
        val sessionToken = remember { mutableStateOf("YOUR_SESSION_TOKEN") }
        val theme = remember { mutableStateOf(defaultTheme) }

        val focusManager = LocalFocusManager.current
        SEONOrchTheme {
            Scaffold { innerPadding ->
                Surface(
                    modifier = Modifier.fillMaxSize()
                        .padding(innerPadding)
                        .pointerInput(Unit) {
                            detectTapGestures(onTap = {
                                focusManager.clearFocus()
                            })
                        },
                    color = MaterialTheme.colorScheme.background
                ) {
                    Column(
                        modifier = Modifier
                            .verticalScroll(rememberScrollState())
                            .padding(20.dp),
                        verticalArrangement = Arrangement.Top,
                        horizontalAlignment = Alignment.CenterHorizontally,
                    ) {
                        Text(
                            text = stringResource(id = R.string.app_name),
                            fontSize = 20.sp
                        )

                        Text(
                            modifier = Modifier
                                .padding(vertical = 24.dp),
                            color = verificationResultTextColor.value,
                            text = String.format(
                                stringResource(id = R.string.verification_flow_result),
                                verificationResultText.value
                            ),
                            fontSize = 16.sp
                        )

                        // Language Section
                        OutlinedTextField(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(bottom = 16.dp)
                                .testTag("language_text_field"),
                            value = language.value,
                            onValueChange = {
                                language.value = it
                            },
                            maxLines = 1,
                            label = { Text("Language (e.g., en, de, fr)") }
                        )

                        // Theme Section
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(bottom = 6.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Checkbox(
                                modifier = Modifier.testTag("customize_theme_checkbox"),
                                checked = customizeTheme.value,
                                onCheckedChange = {
                                    customizeTheme.value = it
                                }
                            )
                            Text(
                                text = "Customize Theme",
                                modifier = Modifier.padding(start = 8.dp)
                            )
                        }

                        OutlinedTextField(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(bottom = 16.dp)
                                .testTag("theme_text_field"),
                            value = theme.value,
                            onValueChange = {
                                theme.value = it
                            },
                            enabled = customizeTheme.value,
                            maxLines = 5,
                            label = { Text("Theme") }
                        )

                        OutlinedTextField(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(bottom = 16.dp)
                                .testTag("session_token_text_field"),
                            value = sessionToken.value,
                            onValueChange = {
                                sessionToken.value = it
                            },
                            maxLines = 1,
                            label = { Text("Session Token") },
                            placeholder = { Text("Paste JWT token here") }
                        )

                        Spacer(modifier = Modifier.weight(1f))

                        Button(
                            modifier = Modifier
                                .padding(top = 32.dp)
                                .testTag("start_verification_button"),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = Color.DarkGray,
                                contentColor = Color.LightGray
                            ),
                            onClick = {
                                buttonClick(
                                    language.value.trim().ifEmpty { null },
                                    if (customizeTheme.value) theme.value.trim().ifEmpty { null } else null,
                                    sessionToken.value.trim().ifEmpty { null }
                                )
                            }
                        ) {
                            Text(text = stringResource(id = R.string.start_verification_button))
                        }
                    }
                }
            }
        }
    }
}
