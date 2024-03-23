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

@Composable
fun LocationUpdatesScreen(viewModel: LocationViewModel) {
    val context = LocalContext.current

    // Setup for handling permissions and location settings.
    SetupPermissionHandling(viewModel, context)

    val locationData by viewModel.locationData.observeAsState()
    val isUpdatingLocation by viewModel.isUpdatingLocation.observeAsState(false)

    ScaffoldWithContent(locationData, isUpdatingLocation, viewModel)
}


@Composable
fun SetupPermissionHandling(viewModel: LocationViewModel, context: Context) {
    // Permission and GPS settings logic...

    val permissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        val allPermissionsGranted = permissions.entries.any { it.value }
        viewModel.onPermissionsResult(allPermissionsGranted)
    }

    val permissionRequestEvent by viewModel.permissionRequestEvent.observeAsState()
    permissionRequestEvent?.getContentIfNotHandled()?.let { permissions ->
        permissionLauncher.launch(permissions)
    }

    val locationSettingsEvent by viewModel.locationSettingsEvent.observeAsState()
    locationSettingsEvent?.getContentIfNotHandled()?.let {
        // Direct users to enable permissions in app settings
        AlertDialogForPermissionActivation(context)
    }

    val showGPSPromptEvent by viewModel.showGPSPromptEvent.observeAsState()
    showGPSPromptEvent?.getContentIfNotHandled()?.let {
        // Prompt users to enable GPS
        AlertDialogForGpsActivation(context)
    }
}


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



@SuppressLint("UnusedMaterialScaffoldPaddingParameter")
@Composable
fun ScaffoldWithContent(
    locationData: LocationData?,
    isUpdatingLocation: Boolean,
    viewModel: LocationViewModel,
) {
    ColumnLayoutForLocationData(locationData, isUpdatingLocation, viewModel)
}

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
        LocationDataDisplay(locationData)
        Spacer(modifier = Modifier.height(UIConstants.SPACING_LARGE.dp))
        ControlButtons(isUpdatingLocation, viewModel)
    }
}


@Composable
fun LocationDataDisplay(locationData: LocationData?) {
    if (locationData != null) {
        Text(text = stringResource(id = R.string.latitude_label) + ": ${locationData.latitude}")
        Spacer(modifier = Modifier.height(UIConstants.SPACING_SMALL.dp))
        Text(text = stringResource(id = R.string.longitude_label) + ": ${locationData.longitude}")
        Spacer(modifier = Modifier.height(UIConstants.SPACING_MEDIUM.dp))
        Text(text = stringResource(id = R.string.last_update_time_label) + ": ${locationData.timestamp}")
    } else {
        Text(text = stringResource(id = R.string.no_location_data))
    }
}



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


