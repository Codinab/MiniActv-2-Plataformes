package com.google.android.gms.location.sample.locationupdates.viewmodel

import android.app.Application
import android.content.Context
import android.location.Location
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.google.android.gms.common.api.ResolvableApiException
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.LocationResult
import com.google.android.gms.location.LocationSettingsRequest
import com.google.android.gms.location.Priority
import com.google.android.gms.location.sample.locationupdates.data.model.LocationData
import kotlinx.coroutines.launch

class LocationViewModel(application: Application) : AndroidViewModel(application) {
    private var fusedLocationClient: FusedLocationProviderClient =
        LocationServices.getFusedLocationProviderClient(application)

    val locationData = MutableLiveData<LocationData?>()
    val isUpdatingLocation = MutableLiveData(false)

    private val permissionGranted = MutableLiveData(false)


    private val locationCallback = object : LocationCallback() {
        override fun onLocationResult(locationResult: LocationResult) {
            locationResult.lastLocation?.let { location ->
                locationData.value = LocationData(
                    latitude = location.latitude,
                    longitude = location.longitude,
                    timestamp = System.currentTimeMillis() // For simplicity, using system time as timestamp
                )
            }
        }
    }


    fun startLocationUpdates() {
        viewModelScope.launch {
            val locationRequest = LocationRequest.Builder(5000) // Fastest update interval in milliseconds
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
}