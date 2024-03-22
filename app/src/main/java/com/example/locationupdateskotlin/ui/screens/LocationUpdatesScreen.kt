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
import androidx.compose.material.Scaffold
import androidx.compose.material.Text
import androidx.compose.material.TopAppBar

import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState

import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.unit.dp
import androidx.compose.ui.res.stringResource
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import com.example.locationupdateskotlin.R
import com.example.locationupdateskotlin.data.model.LocationData
import com.example.locationupdateskotlin.viewmodel.LocationViewModel

@Composable
fun LocationUpdatesScreen(viewModel: LocationViewModel) {
    val context = LocalContext.current

    // Use the lifecycle to manage location updates.
    CheckLocationSettingsOnResume(viewModel)

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
        ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        viewModel.onPermissionsResult(isGranted)
    }

    val permissionRequestEvent by viewModel.permissionRequestEvent.observeAsState()
    permissionRequestEvent?.getContentIfNotHandled()?.let { permission ->
        permissionLauncher.launch(permission)
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
fun CheckLocationSettingsOnResume(viewModel: LocationViewModel) {
    val lifecycleOwner = LocalLifecycleOwner.current
    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_STOP) {
                viewModel.stopLocationUpdates()
            }
        }

        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
        }
    }
}

@Composable
fun AlertDialogForPermissionActivation(context: Context) {
    AlertDialog(
        onDismissRequest = {},
        title = { Text("Location Settings") },
        text = { Text("Your current location settings prevent us from obtaining location information. Please adjust your settings.") },
        confirmButton = {
            Button(onClick = {
                context.startActivity(Intent(ACTION_LOCATION_SOURCE_SETTINGS))
            }) {
                Text("Adjust Settings")
            }
        }
    )
}

@Composable
fun AlertDialogForGpsActivation(context: Context) {
    AlertDialog(
        onDismissRequest = {},
        title = { Text("Enable GPS") },
        text = { Text("GPS is needed for location updates. Please enable GPS.") },
        confirmButton = {
            Button(onClick = {
                //context.startActivity(Intent(ACTION_LOCATION_SOURCE_SETTINGS))
            }) {
                Text("Go to Settings")
            }
        }
    )
}

@SuppressLint("UnusedMaterialScaffoldPaddingParameter")
@Composable
fun ScaffoldWithContent(
    locationData: LocationData?,
    isUpdatingLocation: Boolean,
    viewModel: LocationViewModel,
) {
    Scaffold(
        topBar = {
            TopAppBar(title = { Text("Location Updates") })
        }
    ) {
        ColumnLayoutForLocationData(locationData, isUpdatingLocation, viewModel)
    }
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
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        LocationDataDisplay(locationData)
        Spacer(modifier = Modifier.height(16.dp))
        ControlButtons(
            isUpdatingLocation = isUpdatingLocation,
            onRequestPermissions = {
                // Example toggle or decision mechanism
                val useFineLocation = true // This could be determined by user input or app logic
                viewModel.requestPermissions(useFineLocation)
            },
            onStopClick = {
                viewModel.stopLocationUpdates()
            }
        )
    }
}


@Composable
fun LocationDataDisplay(locationData: LocationData?) {
    if (locationData != null) {
        Text(text = "Latitude: ${locationData.latitude}")
        Spacer(modifier = Modifier.height(4.dp))
        Text(text = "Longitude: ${locationData.longitude}")
        Spacer(modifier = Modifier.height(8.dp))
        Text(text = "Last Update: ${locationData.timestamp}")
    } else {
        Text("No location data available")
    }
}

@Composable
fun ControlButtons(
    isUpdatingLocation: Boolean,
    onRequestPermissions: () -> Unit, // Rename for clarity
    onStopClick: () -> Unit,
) {
    Row {
        Button(
            onClick = onRequestPermissions, // Call ViewModel's requestPermissions
            enabled = !isUpdatingLocation,
            modifier = Modifier.weight(1f)
        ) {
            Text("Start Updates")
        }
        Spacer(modifier = Modifier.width(8.dp))
        Button(
            onClick = onStopClick,
            enabled = isUpdatingLocation,
            modifier = Modifier.weight(1f)
        ) {
            Text(text = stringResource(id = R.string.stop_updates))
        }
    }
}

