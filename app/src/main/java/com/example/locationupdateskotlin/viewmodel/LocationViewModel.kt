package com.example.locationupdateskotlin.viewmodel

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

/**
 * ViewModel responsible for handling location updates in the application.
 * Utilizes Android's FusedLocationProviderClient for efficient location retrieval.
 */
class LocationViewModel(application: Application) : AndroidViewModel(application) {
    // Client for interacting with the fused location provider.
    private var fusedLocationClient: FusedLocationProviderClient =
        LocationServices.getFusedLocationProviderClient(application)

    // LiveData for holding and observing location data.
    val locationData = MutableLiveData<LocationData?>()

    // LiveData to track if location updates are actively being fetched.
    val isUpdatingLocation = MutableLiveData(false)

    // Events for requesting location permissions from the user.
    val permissionRequestEvent = MutableLiveData<Event<Array<String>>>()

    // Event triggered when there is a need to check location settings.
    val locationSettingsEvent = MutableLiveData<Event<Unit>>()

    // Event for prompting the user to enable GPS if it's not enabled.
    val showGPSPromptEvent = MutableLiveData<Event<Unit>>()

    // Client for accessing location settings.
    private val settingsClient: SettingsClient = LocationServices.getSettingsClient(application)

    /**
     * Requests location permissions necessary for fetching location updates.
     */
    fun requestPermissions() {
        val permissions = arrayOf(
            android.Manifest.permission.ACCESS_FINE_LOCATION,
            android.Manifest.permission.ACCESS_COARSE_LOCATION
        )
        permissionRequestEvent.value = Event(permissions)
    }

    // Location request configuration
    private lateinit var locationRequest: LocationRequest

    init {
        createLocationRequest()
    }

    /**
     * Sets up the location request with desired parameters for location updates.
     */
    private fun createLocationRequest() {
        locationRequest = LocationRequest.Builder(FASTEST_UPDATE_INTERVAL_IN_MILLISECONDS)
            .setPriority(Priority.PRIORITY_HIGH_ACCURACY)
            .setWaitForAccurateLocation(false)
            .setMinUpdateIntervalMillis(FASTEST_UPDATE_INTERVAL_IN_MILLISECONDS)
            .setMaxUpdateDelayMillis(UPDATE_INTERVAL_IN_MILLISECONDS)
            .build()
    }

    /**
     * Checks if the device's location settings are satisfied and starts location updates.
     */
    private fun checkLocationSettingsAndStartUpdates() {
        val builder = LocationSettingsRequest.Builder().addLocationRequest(locationRequest)
        val locationSettingsRequest = builder.build()

        settingsClient.checkLocationSettings(locationSettingsRequest)
            .addOnSuccessListener {
                // GPS is enabled, start location updates.
                onGpsEnabledResult(true)
            }
            .addOnFailureListener { e ->
                if (e is ResolvableApiException) {
                    // GPS not enabled, prompt the user to enable it.
                    onGpsEnabledResult(false)
                }
            }
    }

    /**
     * Handles the result from the permission request.
     * @param isGranted True if permissions were granted, false otherwise.
     */
    fun onPermissionsResult(isGranted: Boolean) {
        if (isGranted) {
            checkLocationSettingsAndStartUpdates()
        } else {
            // Permissions were not granted; notify about the requirement.
            locationSettingsEvent.value = Event(Unit)
        }
    }

    /**
     * Handles the GPS enabled state after checking location settings.
     * @param isEnabled True if GPS is enabled, false otherwise.
     */
    private fun onGpsEnabledResult(isEnabled: Boolean) {
        if (isEnabled) {
            startLocationUpdates()
        } else {
            // GPS is not enabled; prompt the user to enable it.
            showGPSPromptEvent.value = Event(Unit)
        }
    }

    /**
     * Callback for receiving location updates.
     */
    private val locationCallback = object : LocationCallback() {
        override fun onLocationResult(locationResult: LocationResult) {
            locationResult.lastLocation?.let { location ->
                // Update the LiveData with the new location data.
                locationData.value = LocationData(
                    latitude = location.latitude,
                    longitude = location.longitude,
                    timestamp = Date()
                )
            }
        }
    }

    /**
     * Starts requesting location updates from the FusedLocationProviderClient.
     */
    private fun startLocationUpdates() {
        viewModelScope.launch {
            try {
                fusedLocationClient.requestLocationUpdates(
                    locationRequest,
                    locationCallback,
                    null
                )
                isUpdatingLocation.value = true
            } catch (e: SecurityException) {
                // Failed to start location updates due to lack of permissions.
                isUpdatingLocation.value = false
            }
        }
    }

    /**
     * Stops receiving location updates.
     */
    fun stopLocationUpdates() {
        fusedLocationClient.removeLocationUpdates(locationCallback)
        isUpdatingLocation.value = false
    }

    /**
     * Called when the ViewModel is being destroyed. Ensures location updates are stopped.
     */
    override fun onCleared() {
        super.onCleared()
        stopLocationUpdates()
    }

    companion object {


        /**
         * The desired interval for location updates. Inexact. Updates may be more or less frequent.
         */
        private const val UPDATE_INTERVAL_IN_MILLISECONDS: Long = 100


        /**
         * The fastest rate for active location updates. Exact. Updates will never be more frequent
         * than this value.
         */
        private const val FASTEST_UPDATE_INTERVAL_IN_MILLISECONDS =
            UPDATE_INTERVAL_IN_MILLISECONDS / 2

    }
}