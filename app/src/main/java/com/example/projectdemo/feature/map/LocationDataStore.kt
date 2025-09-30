package com.example.projectdemo.feature.map

import android.content.Context
import androidx.datastore.preferences.core.*
import androidx.datastore.preferences.preferencesDataStore
import com.google.android.gms.maps.model.LatLng
import com.google.gson.Gson
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

val Context.locationDataStore by preferencesDataStore(name = "location_data")

class LocationDataStore(private val context: Context) {
    private val gson = Gson()

    companion object {
        private val LATITUDE_KEY = doublePreferencesKey("saved_latitude")
        private val LONGITUDE_KEY = doublePreferencesKey("saved_longitude")
        private val ADDRESS_KEY = stringPreferencesKey("saved_address")
        private val IS_UPDATED_KEY = booleanPreferencesKey("is_location_updated")
    }

    suspend fun saveLocation(latLng: LatLng, address: String) {
        context.locationDataStore.edit { preferences ->
            preferences[LATITUDE_KEY] = latLng.latitude
            preferences[LONGITUDE_KEY] = latLng.longitude
            preferences[ADDRESS_KEY] = address
            preferences[IS_UPDATED_KEY] = true
        }
    }

    val savedLocation: Flow<LatLng?> = context.locationDataStore.data
        .map { preferences ->
            val latitude = preferences[LATITUDE_KEY]
            val longitude = preferences[LONGITUDE_KEY]
            if (latitude != null && longitude != null) {
                LatLng(latitude, longitude)
            } else {
                null
            }
        }

    val savedAddress: Flow<String> = context.locationDataStore.data
        .map { preferences ->
            preferences[ADDRESS_KEY] ?: "Location not updated"
        }

    val isLocationUpdated: Flow<Boolean> = context.locationDataStore.data
        .map { preferences ->
            preferences[IS_UPDATED_KEY] ?: false
        }

    suspend fun clearLocation() {
        context.locationDataStore.edit { preferences ->
            preferences.remove(LATITUDE_KEY)
            preferences.remove(LONGITUDE_KEY)
            preferences.remove(ADDRESS_KEY)
            preferences[IS_UPDATED_KEY] = false
        }
    }
}