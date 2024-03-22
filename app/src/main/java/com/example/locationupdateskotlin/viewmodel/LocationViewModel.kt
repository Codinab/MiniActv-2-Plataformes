package com.example.locationupdateskotlin.viewmodel

import android.Manifest.permission.ACCESS_COARSE_LOCATION
import android.Manifest.permission.ACCESS_FINE_LOCATION
import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.example.locationupdateskotlin.data.model.LocationData
import com.example.locationupdateskotlin.util.Event
import com.google.android.gms.common.api.ResolvableApiException
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.LocationSettingsRequest
import com.google.android.gms.location.Priority
import com.google.android.gms.location.SettingsClient
import kotlinx.coroutines.launch
import java.util.Date

class LocationViewModel(application: Application) : AndroidViewModel(application) {
    private var fusedLocationClient: FusedLocationProviderClient =
        LocationServices.getFusedLocationProviderClient(application)

    val locationData = MutableLiveData<LocationData?>()
    val isUpdatingLocation = MutableLiveData(false)

    val permissionRequestEvent = MutableLiveData<Event<String>>()
    val locationSettingsEvent = MutableLiveData<Event<Unit>>()
    val showGPSPromptEvent = MutableLiveData<Event<Unit>>()

    private val settingsClient: SettingsClient = LocationServices.getSettingsClient(application)

    fun requestPermissions(useFineLocation: Boolean) {
        val permission = if (useFineLocation) ACCESS_FINE_LOCATION else ACCESS_COARSE_LOCATION
        permissionRequestEvent.value = Event(permission)
    }


    // Add this line to define locationRequest
    private lateinit var locationRequest: LocationRequest

    init {
        createLocationRequest()
    }

    private fun createLocationRequest() {
        // Adjust these values as necessary for your use case
        locationRequest = LocationRequest.Builder(FASTEST_UPDATE_INTERVAL_IN_MILLISECONDS)
            .setPriority(Priority.PRIORITY_HIGH_ACCURACY)
            .setWaitForAccurateLocation(false)
            .setMinUpdateIntervalMillis(FASTEST_UPDATE_INTERVAL_IN_MILLISECONDS)
            .setMaxUpdateDelayMillis(UPDATE_INTERVAL_IN_MILLISECONDS)
            .build()
    }

    private fun checkLocationSettingsAndStartUpdates() {
        val builder = LocationSettingsRequest.Builder().addLocationRequest(locationRequest)
        val locationSettingsRequest = builder.build()

        settingsClient.checkLocationSettings(locationSettingsRequest)
            .addOnSuccessListener {
                // GPS is enabled, start location updates
                onGpsEnabledResult(true)
            }
            .addOnFailureListener { e ->
                if (e is ResolvableApiException) {
                    // GPS not enabled, try to resolve
                    onGpsEnabledResult(false)
                }
            }
    }
    fun onPermissionsResult(isGranted: Boolean) {
        if (isGranted) {
            checkLocationSettingsAndStartUpdates()
        } else {
            // Logic to handle denial, possibly directing users to app settings
            locationSettingsEvent.value = Event(Unit)
        }
    }

    private fun onGpsEnabledResult(isEnabled: Boolean) {
        if (isEnabled) {
            startLocationUpdates()
        } else {
            // Logic to handle GPS not enabled, prompting users to enable GPS
            showGPSPromptEvent.value = Event(Unit)
        }
    }


    private val locationCallback = object : LocationCallback() {
        override fun onLocationResult(locationResult: LocationResult) {
            locationResult.lastLocation?.let { location ->
                locationData.value = LocationData(
                    latitude = location.latitude,
                    longitude = location.longitude,
                    timestamp = Date()
                )
            }
        }
    }

    fun startLocationUpdates() {
        viewModelScope.launch {
            val locationRequest =
                LocationRequest.Builder(5000) // Fastest update interval in milliseconds
                    .setPriority(Priority.PRIORITY_HIGH_ACCURACY)
                    .setWaitForAccurateLocation(false)
                    .setMinUpdateIntervalMillis(5000) // Minimum update interval in milliseconds
                    .setMaxUpdateDelayMillis(10000) // Update interval in milliseconds
                    .build()
            try {
                fusedLocationClient.requestLocationUpdates(
                    locationRequest,
                    locationCallback,
                    null
                )
                isUpdatingLocation.value = true
            } catch (e: SecurityException) {
                // Handle case where location permissions are not granted
                isUpdatingLocation.value = false
            }
        }
    }

    fun stopLocationUpdates() {
        fusedLocationClient.removeLocationUpdates(locationCallback)
        isUpdatingLocation.value = false
    }

    override fun onCleared() {
        super.onCleared()
        stopLocationUpdates()
    }


    companion object {

        /**
         * Constant used in the location settings dialog.
         */

        private const val REQUEST_CHECK_SETTINGS = 0x1


        /**
         * The desired interval for location updates. Inexact. Updates may be more or less frequent.
         */

        private const val UPDATE_INTERVAL_IN_MILLISECONDS: Long = 100  // 10000


        /**
         * The fastest rate for active location updates. Exact. Updates will never be more frequent
         * than this value.
         */

        private const val FASTEST_UPDATE_INTERVAL_IN_MILLISECONDS =
            UPDATE_INTERVAL_IN_MILLISECONDS / 2

        // Keys for storing activity state in the Bundle.
        private const val KEY_REQUESTING_LOCATION_UPDATES = "requesting-location-updates"
        private const val KEY_LOCATION = "location"
        private const val KEY_LAST_UPDATED_TIME_STRING = "last-updated-time-string"
    }
}