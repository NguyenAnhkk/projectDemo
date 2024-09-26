package com.example.projectdemo.pages.screen

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import coil.compose.rememberAsyncImagePainter
import com.example.projectdemo.R
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query

@Composable
fun UserDetailScreen(
    navController: NavController,
    userId: String
) {
    val firestore = FirebaseFirestore.getInstance()
    val userName = remember { mutableStateOf("") }
    val dateOfBirth = remember { mutableStateOf("") }
    var imageUrl by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(userId) {
        firestore.collection("profile").document(userId).get()
            .addOnSuccessListener { document ->
                userName.value = document.getString("userName") ?: "N/A"
                dateOfBirth.value = document.getString("dateOfBirth") ?: "N/A"
            }

        // Tải ảnh từ Firestore nếu có
        firestore.collection("users").document(userId).collection("images")
            .orderBy("timestamp", Query.Direction.DESCENDING)
            .limit(1)
            .get()
            .addOnSuccessListener { result ->
                if (!result.isEmpty) {
                    imageUrl = result.documents[0].getString("imageUrl")
                }
            }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(
            modifier = Modifier
                .size(100.dp)
                .clip(CircleShape)
                .background(Color.Gray)
        ) {
            when {
                imageUrl != null -> {
                    Image(
                        painter = rememberAsyncImagePainter(model = imageUrl),
                        contentDescription = "User Image",
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxSize()
                    )
                }
                else -> {
                    Image(
                        painter = painterResource(id = R.drawable.defaultimg),
                        contentDescription = "Default Image",
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxSize()
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = "Name: ${userName.value}",
            fontWeight = FontWeight.Bold,
            fontSize = 20.sp
        )
        Text(text = "Date of Birth: ${dateOfBirth.value}")
    }
}

