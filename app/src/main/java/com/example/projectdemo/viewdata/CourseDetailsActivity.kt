package com.example.projectdemo.viewdata

import android.location.Location
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.google.android.gms.maps.model.LatLng
import com.google.firebase.firestore.FirebaseFirestore

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CourseDetailsActivity(
    navController: NavController,
    currentLocation: LatLng,
    dataViewModel: DataViewModel = viewModel()
) {
    val nearbyUsers = remember { mutableStateListOf<User>() }
    val radiusInMeters = 5000f

    LaunchedEffect(Unit) {
        val firestore = FirebaseFirestore.getInstance()
        firestore.collection("location")
            .addSnapshotListener { snapshot, error ->
                if (error != null || snapshot == null) return@addSnapshotListener
                nearbyUsers.clear()
                for (doc in snapshot.documents) {
                    val lat = doc.getDouble("latitude") ?: continue
                    val lng = doc.getDouble("longitude") ?: continue
                    val userId = doc.id
                    val userLocation = LatLng(lat, lng)
                    if (isWithinRadius(currentLocation, userLocation, radiusInMeters)) {
                        firestore.collection("profile").document(userId)
                            .get()
                            .addOnSuccessListener { userDoc ->
                                val userName = userDoc.getString("userName") ?: "N/A"
                                val dateOfBirth = userDoc.getString("dateOfBirth") ?: "N/A"
                                nearbyUsers.add(User(userId, userName, dateOfBirth, userLocation))
                            }
                    }
                }
            }
    }

    Column(
        modifier = Modifier.fillMaxSize().background(Color.White),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        LazyColumn {
            items(nearbyUsers) { user ->
                ElevatedCard(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(8.dp),
                    elevation = CardDefaults.cardElevation(defaultElevation = 6.dp),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFFF1EDE9)),
                    onClick = {
                        navController.navigate("user_detail/${user.userId}")
                    }
                ) {
                    Text(
                        text = "Name: ${user.userName}",
                        color = Color(0xFF117a46),
                        style = TextStyle(fontSize = 20.sp),
                        modifier = Modifier.padding(5.dp)
                    )
                    Text(
                        text = "Date of Birth: ${user.dateOfBirth}",
                        color = Color.Black,
                        style = TextStyle(fontSize = 15.sp),
                        modifier = Modifier.padding(5.dp)
                    )
                }
            }
        }
    }
}

data class User(
    val userId: String,
    val userName: String,
    val dateOfBirth: String,
    val location: LatLng
)

fun isWithinRadius(
    currentLocation: LatLng,
    userLocation: LatLng,
    radiusInMeters: Float
): Boolean {
    val currentLoc = Location("").apply {
        latitude = currentLocation.latitude
        longitude = currentLocation.longitude
    }
    val userLoc = Location("").apply {
        latitude = userLocation.latitude
        longitude = userLocation.longitude
    }
    return currentLoc.distanceTo(userLoc) <= radiusInMeters
}