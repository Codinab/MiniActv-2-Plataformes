package com.example.locationupdateskotlin.data.model

import java.util.Date

/**
 * Represents the location data model containing latitude, longitude, and a timestamp.
 * This data class is intended to encapsulate all relevant details about a location update.
 *
 * @property latitude The latitude of the location.
 * @property longitude The longitude of the location.
 * @property timestamp The timestamp when the location data was received.
 */
data class LocationData(
    val latitude: Double,
    val longitude: Double,
    val timestamp: Date,
)