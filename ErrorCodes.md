# SEON Orchestration Android SDK Error Codes

This section describes the possible error codes returned by the SEON Orchestration Android SDK, along with suggested checks and resolutions for each case.

## ERROR_CODE_1

### Description:

The current session is unrecoverable.

### Resolution:

-   Start a new session by reinitializing the SDK with a fresh configuration.
-   Check if you have provided correct baseUrl, customerData and licenseKey (associated with correct application ID).


## ERROR_CODE_2

### Description:

Failure in fetching session data or initializing the SDK due to missing or incorrect parameters.

### Checklist for Resolution:

-   Verify that the baseUrl is properly provided in the SDK initialization.
-   Confirm that the customerData is correctly set.
-   Check if the CMS API Key is valid and correctly associated:
-   Ensure the CMS API Key matches the Application ID registered with SEON.
-   Ensure that the device has a stable internet connection.


## ERROR_CODE_3

### Description:

The session has expired.

### Typical Cause:

-   Sessions can expire if the verification flow takes an excessively long time to complete.

### Resolution:

-   Restart the verification by creating a new session.
