package com.google.android.gms.location.sample.locationupdates.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material.Button
import androidx.compose.material.Scaffold
import androidx.compose.material.Text
import androidx.compose.material.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.currentCompositionLocalContext
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.res.stringResource
import com.google.android.gms.location.sample.locationupdates.R
import com.google.android.gms.location.sample.locationupdates.data.model.LocationData
import com.google.android.gms.location.sample.locationupdates.viewmodel.LocationViewModel

@Composable
fun LocationUpdatesScreen(
    viewModel: LocationViewModel,
    onRequestPermissions: () -> Unit // This is now a regular Kotlin function
) {
    val locationData by viewModel.locationData.observeAsState()
    val isUpdatingLocation by viewModel.isUpdatingLocation.observeAsState(false)

    Scaffold(
        topBar = {
            TopAppBar(title = { Text("Location Updates") })
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            LocationDataDisplay(locationData)
            Spacer(modifier = Modifier.height(16.dp))
            ControlButtons(
                isUpdatingLocation = isUpdatingLocation,
                onStartClick =
                    // Request permissions when the user decides to start updates
                   onRequestPermissions
                ,
                onStopClick = {
                    viewModel.stopLocationUpdates()
                }
            )
        }
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
    onStartClick: () -> Unit,
    onStopClick: () -> Unit
) {
    Row {
        Button(
            onClick = onStartClick,
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
