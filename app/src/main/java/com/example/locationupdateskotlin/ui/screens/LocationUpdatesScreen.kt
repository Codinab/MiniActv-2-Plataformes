package com.example.locationupdateskotlin.ui.screens

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.provider.Settings.ACTION_LOCATION_SOURCE_SETTINGS
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material.AlertDialog
import androidx.compose.material.Button
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.example.locationupdateskotlin.R
import com.example.locationupdateskotlin.data.model.LocationData
import com.example.locationupdateskotlin.ui.components.UIConstants
import com.example.locationupdateskotlin.viewmodel.LocationViewModel

/**
 * The main screen for displaying location updates and handling permissions.
 * @param viewModel The ViewModel that holds the state and logic for location updates.
 */
@Composable
fun LocationUpdatesScreen(viewModel: LocationViewModel) {
    val context = LocalContext.current

    // Initializes permission handling logic.
    SetupPermissionHandling(viewModel, context)

    val locationData by viewModel.locationData.observeAsState()
    val isUpdatingLocation by viewModel.isUpdatingLocation.observeAsState(false)

    // Displays the main content of the app, including location data and control buttons.
    ScaffoldWithContent(locationData, isUpdatingLocation, viewModel)
}

/**
 * Sets up permission handling, including the launchers for activity results.
 * @param viewModel The ViewModel for accessing and updating location data and state.
 * @param context The current context.
 */
@Composable
fun SetupPermissionHandling(viewModel: LocationViewModel, context: Context) {
    // Handles the flow for requesting multiple permissions.
    val permissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        val allPermissionsGranted = permissions.entries.any { it.value }
        viewModel.onPermissionsResult(allPermissionsGranted)
    }

    // Observes the event for permission requests.
    val permissionRequestEvent by viewModel.permissionRequestEvent.observeAsState()
    permissionRequestEvent?.getContentIfNotHandled()?.let { permissions ->
        permissionLauncher.launch(permissions)
    }

    // Observes the event for location settings checks.
    val locationSettingsEvent by viewModel.locationSettingsEvent.observeAsState()
    locationSettingsEvent?.getContentIfNotHandled()?.let {
        // Shows an alert dialog for permissions activation.
        AlertDialogForPermissionActivation(context)
    }

    // Observes the event for showing a GPS activation prompt.
    val showGPSPromptEvent by viewModel.showGPSPromptEvent.observeAsState()
    showGPSPromptEvent?.getContentIfNotHandled()?.let {
        // Shows an alert dialog for GPS activation.
        AlertDialogForGpsActivation(context)
    }
}

/**
 * Displays an AlertDialog prompting the user to activate permissions for location updates.
 * @param context The current context for starting an Intent to the settings page.
 */
@Composable
fun AlertDialogForPermissionActivation(context: Context) {
    var showDialog by remember { mutableStateOf(true) }

    if (showDialog) {
        AlertDialog(
            onDismissRequest = { showDialog = false },
            title = { Text(text = stringResource(id = R.string.location_settings_title)) },
            text = { Text(text = stringResource(id = R.string.location_settings_message)) },
            confirmButton = {
                Button(onClick = {
                    context.startActivity(Intent(ACTION_LOCATION_SOURCE_SETTINGS))
                }) {
                    Text(text = stringResource(id = R.string.adjust_settings))
                }
            },
            dismissButton = {
                Button(onClick = { showDialog = false }) {
                    Text(text = stringResource(id = R.string.close))
                }
            }
        )
    }
}

/**
 * Displays an AlertDialog prompting the user to enable GPS for location updates.
 * @param context The current context for starting an Intent to the location settings page.
 */
@Composable
fun AlertDialogForGpsActivation(context: Context) {
    var showDialog by remember { mutableStateOf(true) }

    if (showDialog) {
        AlertDialog(
            onDismissRequest = { showDialog = false },
            title = { Text(text = stringResource(id = R.string.enable_gps_title)) },
            text = { Text(text = stringResource(id = R.string.enable_gps_message)) },
            confirmButton = {
                Button(onClick = {
                    context.startActivity(Intent(ACTION_LOCATION_SOURCE_SETTINGS))
                }) {
                    Text(text = stringResource(id = R.string.go_to_settings))
                }
            },
            dismissButton = {
                Button(onClick = { showDialog = false }) {
                    Text(text = stringResource(id = R.string.close))
                }
            }
        )
    }
}

/**
 * Scaffold wrapper for content displaying location data and control buttons.
 * @param locationData The current location data to display.
 * @param isUpdatingLocation Whether the app is currently updating location.
 * @param viewModel The ViewModel for controlling location updates.
 */
@SuppressLint("UnusedMaterialScaffoldPaddingParameter")
@Composable
fun ScaffoldWithContent(
    locationData: LocationData?,
    isUpdatingLocation: Boolean,
    viewModel: LocationViewModel,
) {
    // Layout for displaying location data and control buttons.
    ColumnLayoutForLocationData(locationData, isUpdatingLocation, viewModel)
}

/**
 * Defines the column layout for displaying location data and control buttons.
 * @param locationData The location data to display.
 * @param isUpdatingLocation Whether location updates are active.
 * @param viewModel The ViewModel for controlling location updates.
 */
@Composable
fun ColumnLayoutForLocationData(
    locationData: LocationData?,
    isUpdatingLocation: Boolean,
    viewModel: LocationViewModel,
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(UIConstants.PADDING_STANDARD.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        // Displays the current location data.
        LocationDataDisplay(locationData)
        Spacer(modifier = Modifier.height(UIConstants.SPACING_LARGE.dp))
        // Displays control buttons for starting/stopping location updates.
        ControlButtons(isUpdatingLocation, viewModel)
    }
}

/**
 * Displays the current location data.
 * @param locationData The location data to display.
 */
@Composable
fun LocationDataDisplay(locationData: LocationData?) {
    if (locationData != null) {
        // Displaying latitude, longitude, and last update time if location data is available.
        Text(text = stringResource(id = R.string.latitude_label) + ": ${locationData.latitude}")
        Spacer(modifier = Modifier.height(UIConstants.SPACING_SMALL.dp))
        Text(text = stringResource(id = R.string.longitude_label) + ": ${locationData.longitude}")
        Spacer(modifier = Modifier.height(UIConstants.SPACING_MEDIUM.dp))
        Text(text = stringResource(id = R.string.last_update_time_label) + ": ${locationData.timestamp}")
    } else {
        // Displaying a message when no location data is available.
        Text(text = stringResource(id = R.string.no_location_data))
    }
}


/**
 * Defines control buttons for starting and stopping location updates.
 * @param isUpdatingLocation Whether location updates are currently being fetched.
 * @param viewModel The ViewModel to control the location updates.
 **/
@Composable
fun ControlButtons(
    isUpdatingLocation: Boolean,
    viewModel: LocationViewModel,
) {
    Row {
        Button(
            onClick = {
                viewModel.requestPermissions()
            },
            enabled = !isUpdatingLocation,
            modifier = Modifier.weight(1f)
        ) {
            Text(text = stringResource(id = R.string.start_updates))
        }
        Spacer(modifier = Modifier.width(8.dp))
        Button(
            onClick = {
                viewModel.stopLocationUpdates()
            },
            enabled = isUpdatingLocation,
            modifier = Modifier.weight(1f)
        ) {
            Text(text = stringResource(id = R.string.stop_updates))
        }
    }
}


