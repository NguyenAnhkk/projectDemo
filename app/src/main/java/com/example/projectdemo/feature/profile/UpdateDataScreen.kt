package com.example.projectdemo.feature.profile

import android.annotation.SuppressLint
import android.content.Context
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.example.projectdemo.feature.course.Course
import com.google.firebase.firestore.FirebaseFirestore

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun UpdateDataScreen(name: String, age: String, address: String) {
    var updatedName by remember { mutableStateOf(name) }
    var updatedAge by remember { mutableStateOf(age) }
    var updatedAddress by remember { mutableStateOf(address) }
    val context = LocalContext.current
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        TextField(
            value = updatedName,
            onValueChange = { updatedName = it },
            label = { Text("Name") },
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp)
        )
        TextField(
            value = updatedAge,
            onValueChange = { updatedAge = it },
            label = { Text("Age") },
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp)
        )
        TextField(
            value = updatedAddress,
            onValueChange = { updatedAddress = it },
            label = { Text("Address") },
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp)
        )
        Button(
            onClick = {
                if (updatedName.isEmpty() || updatedAge.isEmpty() || updatedAddress.isEmpty()) {
                    Toast.makeText(context, "Name , Age , Address not null", Toast.LENGTH_SHORT)
                        .show()
                } else {
                    updateDataToFirebase(
                        updatedName,
                        updatedAge,
                        updatedAddress, context
                    )
                }

            }, modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Text(text = "Update Data", modifier = Modifier.padding(8.dp))
        }
        Spacer(modifier = Modifier.height(10.dp))
        Button(
            onClick = { deleteDataFromFirebase(name ,context) }, modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Text(text = "Delete Course", modifier = Modifier.padding(8.dp))
        }
    }
}

@SuppressLint("SuspiciousIndentation")
fun updateDataToFirebase(
    name: String,
    age: String,
    address: String,
    context: Context
) {
    val db: FirebaseFirestore = FirebaseFirestore.getInstance()
    val updateCourses = Course(name, age, address)
    db.collection("Courses").document(name).set(updateCourses)
        .addOnSuccessListener {
            Toast.makeText(
                context,
                "Your Course has been added to Firebase Firestore",
                Toast.LENGTH_SHORT
            ).show()
        }.addOnFailureListener { e ->
            Toast.makeText(context, "Fail to add course \n$e", Toast.LENGTH_SHORT).show()
        }

}

fun deleteDataFromFirebase(name : String, context: Context) {
    val db = FirebaseFirestore.getInstance()
    db.collection("Courses").document(name)
        .delete()
        .addOnSuccessListener {
            Toast.makeText(
                context,
                "Course Deleted successfully..",
                Toast.LENGTH_SHORT
            ).show()
        }
        .addOnFailureListener { e ->
            Toast.makeText(context, "Fail to delete course..", Toast.LENGTH_SHORT).show()
        }

}





















