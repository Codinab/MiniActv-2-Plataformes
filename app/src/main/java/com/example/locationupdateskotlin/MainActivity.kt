package com.example.locationupdateskotlin

import android.Manifest
import android.content.Intent
import android.os.Bundle
import android.provider.Settings.ACTION_LOCATION_SOURCE_SETTINGS
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import com.example.locationupdateskotlin.ui.screens.LocationUpdatesScreen
import com.example.locationupdateskotlin.viewmodel.LocationViewModel

class MainActivity : ComponentActivity() {

    private val locationViewModel: LocationViewModel by viewModels()

    // ActivityResultLauncher for handling permission requests
    private val locationPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted: Boolean ->
        if (isGranted) {
            locationViewModel.startLocationUpdates()
        } else {
            requestPermissions()
            Log.d(TAG, "Permissions denied")
            //Permissions denied
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        locationViewModel.showGPSPromptEvent.observe(this) { event ->
            event.getContentIfNotHandled()?.let {
                // Show dialog to adjust location settings
                val intent = Intent(ACTION_LOCATION_SOURCE_SETTINGS)
                startActivity(intent)
            }
        }

        setContent {
            androidx.compose.material.MaterialTheme {
                androidx.compose.material.Surface {
                    LocationUpdatesScreen(
                        viewModel = locationViewModel
                    )
                }
            }
        }
    }

    private fun requestPermissions() {
        val shouldProvideRationale =
            shouldShowRequestPermissionRationale(Manifest.permission.ACCESS_FINE_LOCATION)

        if (shouldProvideRationale) {
            Log.i(TAG, "Displaying permission rationale to provide additional context.")
            locationPermissionLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION)
        } else {
            Log.i(TAG, "Requesting permission")
            locationPermissionLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION)
        }
    }

    companion object {
        private val TAG = MainActivity::class.java.simpleName


    }
}