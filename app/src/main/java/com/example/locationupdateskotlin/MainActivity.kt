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

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
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
}