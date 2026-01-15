# SEON Orchestration Android SDK

## Description

SEON Orchestration Android SDK is a framework designed to streamline the Fraud check and Identity Verification process in your applications. This SDK facilitates integrating SEON's Fraud, Device Fingerprint and ID Verification capabilities into your Android app seamlessly.

**Owner Team:** SEON Technologies Ltd.

## Installation

SEONOrchSDK is available through Maven Central Repository. To install it, follow these steps:

##### If you are using kotlin version catalogs

- Add this into your project level (root) `settings.gradle.kts`:

```
dependencyResolutionManagement {
    repositories {
        ...
        mavenCentral()
    }
}
```
- Add these lines under the proper section in the `libs.versions.toml`:

```
[versions]
...
seonOrchSDK = "0.1.0"
...
[libraries]
...
seon-orchestration = { group = "io.seon.orchSDK", name = "orchestration-sdk-android", version.ref = "seonOrchSDK" }
```

- And add this into your module level (eg. app) `build.gradle.kts`:

```
dependencies {
    ...
    implementation(libs.seon.orchestration)
}
```

- You can now run gradle sync.

------------

##### If you are using older gradle dependency management
*(syntax may be vary for gradle or gradle.kts files)*

- Add this line to the project level (root) `build.gradle(.kts)`:

```
allprojects {
    repositories {
        ...
        mavenCentral()
    }
}
```

- And add this line to the module level (eg. app) `build.gradle(.kts)`:

```
dependencies {
    ...
    implementation "io.seon.orchSDK:orchestration-sdk-android:0.1.0"
}
```

- You can now run gradle sync.

------------

- To integrate the SDK first thing you have to do is init the activity result launcher (before onResume() is called), so you could do it initially in your activity:

```
private val activityResultLauncher =
    registerForActivityResult(StartActivityForResult()) { result: ActivityResult ->
        handleResult(result)
    }
```

- Next thing is to initialize the SDK through OrchestrationService class (singleton) initialize(...) method:

```
OrchestrationService.instance.initialize(
    baseUrl = "https://example.url/", // use the baseUrl you received from SEON
    token = token, // The token which you got from your own Service endpoint, once you have already authenticated your required workflow and receievd the token
    languageCode = "en" // use the language iso code you prefer to use in the SDK. If not provided, we fall back on the default Locale language of the app, and finally on our default language, which is English (for the available languages please contact SEON)
    theme = theme // This is the same theme object (encoded as a string which is a JSON) and is supposed to be applied as a style over your workflow. For more information, visit the workflow Initialization docs which your service is using to start a workflow.
)
```

- And now you can start its verification flow by calling startVerificationFlow(...) method of the OrchestrationService class:

```
OrchestrationService.instance.startVerificationFlow(
    activityResultLauncher,
    applicationContext
)
```

- Also you have to add these permissions and features to your app's AndroidManifest.xml file. Please notice that since the Verification process is happening in a WebView context, you need some extra permissions due to limitations in the WebView.

```
<uses-permission android:name="android.permission.INTERNET" />

<uses-permission android:name="android.permission.CAMERA" />

<uses-permission android:name="android.permission.WRITE_EXTERNAL_STORAGE"
    android:maxSdkVersion="28" />

<uses-permission android:name="android.permission.READ_EXTERNAL_STORAGE"
    android:maxSdkVersion="32" />
    
<uses-permission android:name="android.permission.RECORD_AUDIO" />

<uses-permission android:name="android.permission.MODIFY_AUDIO_SETTINGS" />

<uses-feature
    android:name="android.hardware.camera"
    android:required="true" />
    
<uses-feature android:name="android.hardware.camera.autofocus"/>

// In case you need GeoLocation-Based Fraud Check API in the Workflow Configuration:

<uses-permission android:name="android.permission.ACCESS_FINE_LOCATION" />

```

- Lastly you have to implement the handler method called by the activityResultLauncher:

```
private fun handleResult(result: ActivityResult) {
    when (result.resultCode) {
        SEONOrchFlowResult.InterruptedByUser.code -> {
            // user navigated back
        }

        SEONOrchFlowResult.Error.code -> {
            // handle error, its code is given in the bundle under the following key
            val error = result.data?.getStringExtra(VERIFICATION_ERROR_KEY)
        }

        SEONOrchFlowResult.Completed.code -> {
            // handle completed status
        }
        
        SEONOrchFlowResult.CompletedSuccess.code -> {
            // handle completed success status
        }
        
        SEONOrchFlowResult.CompletedPending.code -> {
            // handle completed pending status
        }
        
        SEONOrchFlowResult.CompletedFailed.code -> {
            // handle completed failed status
        }
        
        SEONOrchFlowResult.MissingLocationPermission.code -> {
            // handle location permission gracefully based on your needs
        }
    }
}
```

- For security reasons, the SDK may not work on emulator as expected. Please use real devices for using the SEON Orch SDK.

- For the error codes and how to resolve them, please refer to [this page](ErrorCodes.md).

# Changelog

## 0.1.0
-   Initial Version
