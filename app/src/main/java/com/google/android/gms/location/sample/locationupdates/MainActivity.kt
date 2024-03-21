package com.google.android.gms.location.sample.locationupdates

import android.Manifest.permission.ACCESS_FINE_LOCATION
import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Surface
import com.google.android.gms.location.sample.locationupdates.ui.screens.LocationUpdatesScreen
import com.google.android.gms.location.sample.locationupdates.viewmodel.LocationViewModel
import com.google.android.material.snackbar.Snackbar

class MainActivity : ComponentActivity() {

    private val locationViewModel: LocationViewModel by viewModels()

    // ActivityResultLauncher for handling permission requests
    private val locationPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted: Boolean ->
        if (isGranted) {
            locationViewModel.startLocationUpdates()
        } else {
            // Handle the case where permission is denied
            showSnackbar(R.string.permission_denied_explanation, R.string.settings, View.OnClickListener {
                // Action to take when the snackbar action is clicked
            })
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            MaterialTheme {
                Surface {
                    LocationUpdatesScreen(
                        viewModel = locationViewModel, onRequestPermissions =
                        { requestPermissions() }
                    )
                }
            }
        }
    }

    private fun requestPermissions() {
        val shouldProvideRationale = shouldShowRequestPermissionRationale(ACCESS_FINE_LOCATION)

        if (shouldProvideRationale) {
            Log.i(TAG, "Displaying permission rationale to provide additional context.")
            showSnackbar(
                R.string.permission_rationale, android.R.string.ok,
                View.OnClickListener {
                    locationPermissionLauncher.launch(ACCESS_FINE_LOCATION)
                }
            )
        } else {
            Log.i(TAG, "Requesting permission")
            locationPermissionLauncher.launch(ACCESS_FINE_LOCATION)
        }
    }

    private fun showSnackbar(mainTextStringId: Int, actionStringId: Int, listener: View.OnClickListener) {
        Snackbar.make(
            findViewById(android.R.id.content),
            getString(mainTextStringId),
            Snackbar.LENGTH_INDEFINITE
        ).setAction(getString(actionStringId), listener).show()
    }

    companion object {
        private val TAG = MainActivity::class.java.simpleName
    }
}

