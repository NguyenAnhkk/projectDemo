package com.example.projectdemo.feature.map

import android.content.Context
import android.location.Address
import android.location.Geocoder
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import com.google.android.gms.maps.model.LatLng
import java.util.Locale

object ManagerLocation {
    var currentLocationState by mutableStateOf<LatLng?>(null)
    var isLocationUpdated by mutableStateOf(false)
    var currentAddress by mutableStateOf("Vị trí chưa cập nhật")

    fun updateLocation(latLng: LatLng, context: Context) {
        currentLocationState = latLng
        isLocationUpdated = true
        currentAddress = getAddressFromLatLng(latLng, context)
    }

    private fun getAddressFromLatLng(latLng: LatLng, context: Context): String {
        val geocoder = Geocoder(context, Locale.getDefault())
        return try {
            val addresses: List<Address>? = geocoder.getFromLocation(latLng.latitude, latLng.longitude, 1)
            if (addresses.isNullOrEmpty()) {
                "Không xác định"
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
            "Không thể xác định địa chỉ"
        }
    }
}