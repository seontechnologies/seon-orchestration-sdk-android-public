package io.seon.id_verification

import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.WindowInsets
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.ActivityResult
import androidx.activity.result.contract.ActivityResultContracts.StartActivityForResult
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.ArrowDropDown
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.DisplayMode
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SelectableDates
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import io.seon.id_verification_android.pub.Constants.VERIFICATION_ERROR_KEY
import io.seon.id_verification_android.pub.IDVService
import io.seon.id_verification_android.pub.IDVFlowResult
import androidx.compose.ui.unit.sp
import io.seon.id_verification.ui.theme.SeonidverificationTheme
import io.seon.id_verification_android.pub.DateOfBirth
import io.seon.id_verification_android.pub.IDVCustomerData
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.time.format.FormatStyle
import java.util.Locale
import java.util.UUID

class MainActivity : ComponentActivity() {

    private val verificationResultText = mutableStateOf("")
    private val verificationResultTextColor = mutableStateOf(Color.Unspecified)
    private val verificationActivityResultLauncher =
        registerForActivityResult(StartActivityForResult()) { result: ActivityResult ->
            handleVerificationResult(result)
        }
    private val idvService = IDVService.instance
    private var isNavigating = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
//        WindowCompat.setDecorFitsSystemWindows(window, false)
//        idvService.setThemeColors(
//            textOnLight = Pair(getColor(R.color.black), getColor(R.color.white)),
//            textOnDark = Pair(getColor(R.color.white), getColor(R.color.black)),
//            accent = Pair(getColor(R.color.purple_200), getColor(R.color.purple_700)),
//            onAccent = Pair(getColor(R.color.black), getColor(R.color.white)),
//        )
//        idvService.setCustomFonts(
//            Font(R.font.comic_neue_regular),
//            Font(R.font.comic_neue_italic),
//            Font(R.font.comic_neue_bold)
//        )
//        idvService.setWatermarkImageVisibility(false)
//        val drawable = getDrawable(R.drawable.gb_solutions_zrt_logo)
//        if (drawable != null) {
//            idvService.setWatermarkImage(drawable)
//        }
        setContent {
            SampleContent(buttonClick = { baseUrl, licenceKey, countryISOCode, templateId, name, dateOfBirth, address, postalCode ->
                if (!isNavigating) {
                    idvService.initialize(
                        baseUrl = baseUrl,
                        customerData = IDVCustomerData(
                            licenseKey = licenceKey,
                            referenceId = UUID.randomUUID().toString(),
                            countryISOCode = countryISOCode,
                            name = name,
                            dateOfBirth = dateOfBirth,
                            address = address,
                            postalCode = postalCode
                        ),
                        templateId = templateId,
                    )
                    idvService.startVerificationFlow(
                        verificationActivityResultLauncher,
                        applicationContext
                    )
                    isNavigating = true
                    Handler(Looper.getMainLooper()).postDelayed({
                        isNavigating = false
                    }, 500L)
                }
            })
        }
    }

    private fun handleVerificationResult(result: ActivityResult) {
        when (result.resultCode) {

            IDVFlowResult.InterruptedByUser.code -> {
                verificationResultTextColor.value = Color(red = 220, green = 120, blue = 0)
                verificationResultText.value =
                    IDVFlowResult.InterruptedByUser.name
            }

            IDVFlowResult.Error.code -> {
                val error = result.data?.getStringExtra(VERIFICATION_ERROR_KEY)
                verificationResultTextColor.value = Color(red = 178, green = 34, blue = 34)
                val trimmedError : String = when (error?.isNotEmpty()) {
                    true -> error
                    else -> IDVFlowResult.Error.name
                }
                verificationResultText.value = trimmedError
            }

            IDVFlowResult.Completed.code -> {
                verificationResultTextColor.value = Color(red = 34, green = 139, blue = 34)
                verificationResultText.value =
                    IDVFlowResult.Completed.name
            }

            IDVFlowResult.CompletedSuccess.code -> {
                verificationResultTextColor.value = Color(red = 34, green = 139, blue = 34)
                verificationResultText.value =
                    IDVFlowResult.CompletedSuccess.name
            }

            IDVFlowResult.CompletedPending.code -> {
                verificationResultTextColor.value = Color(red = 34, green = 139, blue = 34)
                verificationResultText.value =
                    IDVFlowResult.CompletedPending.name
            }

            IDVFlowResult.CompletedFailed.code -> {
                verificationResultTextColor.value = Color(red = 178, green = 34, blue = 34)
                verificationResultText.value =
                    IDVFlowResult.CompletedFailed.name
            }
        }
    }

    @OptIn(ExperimentalMaterial3Api::class)
    @Composable
    fun SampleContent(buttonClick: (
        baseUrl: String,
        licenseKey: String,
        countryISOCode: String?,
        templateId: String?,
        name: String?,
        dateOfBirth: DateOfBirth?,
        address: String?,
        postalCode: String?
    ) -> Unit) {

        val baseUrl = remember { mutableStateOf("https://idv-eu.seon.io/") }
        val expanded = remember { mutableStateOf(false) }
        val licenseKeys = mapOf(
            Pair("Selfie", ""),
            Pair("Hand Gesture", ""),
            Pair("Face Rotation", "")
        )
        val selectedLicenseKey = remember { mutableStateOf(Pair(licenseKeys.keys.first(), licenseKeys.values.first())) }
        val countryISOCode = remember { mutableStateOf("") }
        val templateId = remember { mutableStateOf("") }
        val name = remember { mutableStateOf("") }
        val address = remember { mutableStateOf("") }
        val postalCode = remember { mutableStateOf("") }
        val focusManager = LocalFocusManager.current
        val today = LocalDate.now(ZoneId.systemDefault())
        val defaultDate = today.minusYears(20)
        val minDate = today.minusYears(150)
        val maxDate = today
        var showDialog = remember { mutableStateOf(false) }
        val defaultMillis = defaultDate.toEpochMilli()
        val datePickerState = rememberDatePickerState(
            initialSelectedDateMillis = defaultMillis,
            selectableDates = object : SelectableDates {
                override fun isSelectableDate(utcTimeMillis: Long): Boolean {
                    val date = LocalDate.ofEpochDay(utcTimeMillis / (24 * 60 * 60 * 1000))
                    return !date.isBefore(minDate) && !date.isAfter(maxDate)
                }
            },
            initialDisplayMode = DisplayMode.Picker
        )
        var selectedDate : MutableState<DateOfBirth?> = remember { mutableStateOf(null) }
        var selectedDateToShow : MutableState<String?> = remember { mutableStateOf(null) }
        SeonidverificationTheme {
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

                        OutlinedTextField(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(bottom = 16.dp),
                            value = baseUrl.value,
                            maxLines = 1,
                            onValueChange = {
                                baseUrl.value = it
                            },
                            label = { Text(stringResource(id = R.string.base_url)) }
                        )

                        Box(
                            Modifier
                                .fillMaxWidth()
                                .padding(bottom = 16.dp)
                        ) {
                            Column(
                                verticalArrangement = Arrangement.Top,
                                horizontalAlignment = Alignment.CenterHorizontally,
                            ) {
                                OutlinedTextField(
                                    modifier = Modifier
                                        .fillMaxWidth(),
                                    value = (selectedLicenseKey.value.first),
                                    onValueChange = { },
                                    label = { Text(stringResource(id = R.string.liveness_type)) },
                                    trailingIcon = { Icon(Icons.Outlined.ArrowDropDown, null) },
                                    readOnly = true
                                )
                                DropdownMenu(
                                    modifier = Modifier
                                        .fillMaxWidth(),
                                    expanded = expanded.value,
                                    onDismissRequest = { expanded.value = false }
                                ) {
                                    licenseKeys.forEach {
                                        DropdownMenuItem(
                                            onClick = {
                                                selectedLicenseKey.value = Pair(it.key, it.value)
                                                expanded.value = false
                                            },
                                            text = {
                                                Text(
                                                    fontSize = 16.sp,
                                                    text = (it.key)
                                                )
                                            }
                                        )
                                    }
                                }
                            }
                            Spacer(
                                modifier = Modifier
                                    .matchParentSize()
                                    .background(Color.Transparent)
                                    .padding(start = 20.dp, end = 20.dp, top = 16.dp)
                                    .clickable(
                                        onClick = { expanded.value = !expanded.value }
                                    )
                            )
                        }

                        OutlinedTextField(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(bottom = 16.dp),
                            value = selectedLicenseKey.value.second,
                            maxLines = 1,
                            onValueChange = { newValue ->
                                if (selectedLicenseKey.value.second != newValue) {
                                    selectedLicenseKey.value = Pair(
                                        licenseKeys.entries.find { it.value == newValue }?.key
                                            ?: "-", newValue
                                    )
                                }
                            },
                            label = { Text(stringResource(id = R.string.license_key)) }
                        )

                        OutlinedTextField(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(bottom = 16.dp),
                            value = countryISOCode.value,
                            maxLines = 1,
                            onValueChange = {
                                if (it.length <= 2) {
                                    countryISOCode.value = it
                                }
                            },
                            label = { Text(stringResource(id = R.string.country_iso_code)) },
                            keyboardOptions = KeyboardOptions(
                                capitalization = KeyboardCapitalization.Characters
                            )
                        )

                        OutlinedTextField(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(bottom = 16.dp),
                            value = templateId.value,
                            maxLines = 1,
                            onValueChange = {
                                templateId.value = it
                            },
                            label = { Text(stringResource(id = R.string.template_id)) }
                        )

                        OutlinedTextField(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(bottom = 16.dp),
                            value = name.value,
                            maxLines = 1,
                            onValueChange = {
                                name.value = it
                            },
                            label = { Text(stringResource(id = R.string.name)) }
                        )

                        if (showDialog.value) {
                            DatePickerDialog(
                                onDismissRequest = { showDialog.value = false },
                                confirmButton = {
                                    TextButton(
                                        onClick = {
                                            selectedDate.value =
                                                convertToDOB(datePickerState.selectedDateMillis)
                                            selectedDateToShow.value =
                                                formatDateToLocale(datePickerState.selectedDateMillis)
                                            showDialog.value = false
                                        }
                                    ) {
                                        Text("OK")
                                    }
                                },
                                dismissButton = {
                                    TextButton(
                                        onClick = { showDialog.value = false }
                                    ) {
                                        Text("Cancel")
                                    }
                                }
                            ) {
                                DatePicker(
                                    state = datePickerState,
                                )
                            }
                        }

                        OutlinedButton(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(80.dp)
                                .padding(top = 8.dp, bottom = 16.dp),
                            contentPadding = PaddingValues(16.dp),
                            shape = RoundedCornerShape(5.dp),
                            onClick = {
                                showDialog.value = !showDialog.value
                            }) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    selectedDateToShow.value
                                        ?: stringResource(id = R.string.date_of_birth),
                                    modifier = Modifier
                                        .weight(1f),
                                    textAlign = TextAlign.Start,
                                    fontSize = 16.sp,
                                )
                                if (selectedDate.value != null) {
                                    TextButton(
                                        modifier = Modifier
                                            .width(40.dp)
                                            .height(30.dp),
                                        onClick = {
                                            selectedDate.value = null
                                            selectedDateToShow.value = null
                                        },
                                        contentPadding = PaddingValues(0.dp)
                                    ) {
                                        Text("Clear")
                                    }
                                }
                            }
                        }

                        OutlinedTextField(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(bottom = 16.dp),
                            value = address.value,
                            onValueChange = {
                                address.value = it
                            },
                            label = { Text(stringResource(id = R.string.address)) }
                        )

                        OutlinedTextField(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(bottom = 16.dp),
                            value = postalCode.value,
                            onValueChange = {
                                postalCode.value = it
                            },
                            maxLines = 1,
                            label = { Text(stringResource(id = R.string.postal_code)) }
                        )

                        Spacer(modifier = Modifier.weight(1f))

                        Button(
                            modifier = Modifier
                                .padding(top = 32.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = Color.DarkGray,
                                contentColor = Color.LightGray
                            ),
                            onClick = {
                                buttonClick(
                                    baseUrl.value.trim(),
                                    selectedLicenseKey.value.second.trim(),
                                    countryISOCode.value.trim().ifEmpty { null },
                                    templateId.value.trim().ifEmpty { null },
                                    name.value.trim().ifEmpty { null },
                                    selectedDate.value,
                                    address.value.trim().ifEmpty { null },
                                    postalCode.value.trim().ifEmpty { null }
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

    private fun convertToDOB(millis: Long?): DateOfBirth? {
        millis?.let {
            Instant.ofEpochMilli(it).atZone(ZoneId.systemDefault()).toLocalDate()
        }?.let {
            return DateOfBirth(day = it.dayOfMonth, month = it.monthValue, year = it.year)
        } ?: return null
    }

    private fun formatDateToLocale(millis: Long?, locale: Locale = Locale.getDefault()): String? {
        return millis?.let {
            val formatter = DateTimeFormatter.ofLocalizedDate(FormatStyle.SHORT).withLocale(locale)
            val date = Instant.ofEpochMilli(it).atZone(ZoneId.systemDefault()).toLocalDate()
            return date.format(formatter)
        }
    }
}

fun LocalDate.toEpochMilli(): Long {
    return this.atStartOfDay(ZoneId.systemDefault())
        .toInstant()
        .toEpochMilli()
}