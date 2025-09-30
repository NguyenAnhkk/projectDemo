package com.example.projectdemo.feature.map

import android.content.Context
import android.location.Address
import android.location.Geocoder
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import com.google.android.gms.maps.model.LatLng
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.util.Locale

object ManagerLocation {
    var currentLocationState by mutableStateOf<LatLng?>(null)
    var isLocationUpdated by mutableStateOf(false)
    var currentAddress by mutableStateOf("Location not updated")

    private lateinit var locationDataStore: LocationDataStore

    fun initialize(context: Context) {
        locationDataStore = LocationDataStore(context)
        loadSavedLocation(context)
    }

    private fun loadSavedLocation(context: Context) {
        CoroutineScope(Dispatchers.IO).launch {
            locationDataStore.savedLocation.collect { latLng ->
                latLng?.let {
                    currentLocationState = it
                }
            }
        }

        CoroutineScope(Dispatchers.IO).launch {
            locationDataStore.savedAddress.collect { address ->
                currentAddress = address
            }
        }

        CoroutineScope(Dispatchers.IO).launch {
            locationDataStore.isLocationUpdated.collect { updated ->
                isLocationUpdated = updated
            }
        }
    }

    fun updateLocation(latLng: LatLng, context: Context) {
        currentLocationState = latLng
        isLocationUpdated = true
        val address = getAddressFromLatLng(latLng, context)
        currentAddress = address

        // Lưu vào DataStore
        CoroutineScope(Dispatchers.IO).launch {
            locationDataStore.saveLocation(latLng, address)
        }
    }

    private fun getAddressFromLatLng(latLng: LatLng, context: Context): String {
        val geocoder = Geocoder(context, Locale.getDefault())
        return try {
            val addresses: List<Address>? = geocoder.getFromLocation(latLng.latitude, latLng.longitude, 1)
            if (addresses.isNullOrEmpty()) {
                "Unknown"
            } else {
                val addressLine = addresses[0].getAddressLine(0)
                val addressParts = addressLine.split(",")
                if (addressParts.size >= 2) {
                    addressParts[0] + "," + addressParts[1]
                } else {
                    addressLine
                }
            }
        } catch (e: Exception) {
            "Unable to determine address"
        }
    }

    suspend fun clearLocation() {
        currentLocationState = null
        isLocationUpdated = false
        currentAddress = "Location not updated"
        locationDataStore.clearLocation()
    }


    fun getCurrentLocation(): LatLng {
        return currentLocationState ?: LatLng(21.0278, 105.8342)
    }
}