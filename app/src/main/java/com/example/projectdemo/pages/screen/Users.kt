package com.example.projectdemo.pages.screen

import android.content.Context
import android.text.TextUtils
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api

import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.projectdemo.viewdata.Course
import com.example.projectdemo.ui.theme.AuthViewModel
import com.google.firebase.firestore.CollectionReference
import com.google.firebase.firestore.FirebaseFirestore
import androidx.compose.material3.Text
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.sp

@Composable
fun Users(
    modifier: Modifier = Modifier,
    navController: NavController,
    authViewModel: AuthViewModel
) {
    firebaseUI(LocalContext.current, navController)
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun firebaseUI(context: Context, navController: NavController) {
    val name = remember {
        mutableStateOf("")
    }
    var age = remember {
        mutableStateOf("")
    }
    val address = remember {
        mutableStateOf("")
    }
    Column(
        modifier = Modifier
            .fillMaxHeight()
            .fillMaxWidth()
            .background(Color.White),
        verticalArrangement = Arrangement.Center, horizontalAlignment = Alignment.CenterHorizontally
    ) {
        TextField(
            value = name.value,
            onValueChange = {
                if (it.length <= 50) {
                    name.value = it
                }
            },
            placeholder = { Text(text = "Enter your name") },
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth(),
            textStyle = TextStyle(color = Color.Black, fontSize = 15.sp),
            singleLine = true,
        )

        Spacer(modifier = Modifier.height(10.dp))

        TextField(
            value = age.value,
            onValueChange = {
                val newValue = it.filter { char -> char.isDigit() }
                if (newValue.isNotEmpty() && newValue.length <= 3) {
                    val ageValue = newValue.toIntOrNull()
                    if (ageValue != null && ageValue <= 150) {
                        age.value = newValue
                    }
                } else {
                    age.value = ""
                }
            },
            placeholder = { Text(text = "Enter your age") },
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth(),
            textStyle = TextStyle(color = Color.Black, fontSize = 15.sp),
            singleLine = true,
            keyboardOptions = KeyboardOptions.Default.copy(keyboardType = KeyboardType.Number)
        )

        Spacer(modifier = Modifier.height(10.dp))

        TextField(
            value = address.value,
            onValueChange = { if(it.length <= 100 ){address.value = it} },
            placeholder = { Text(text = "Enter your address") },
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth(),
            textStyle = TextStyle(color = Color.Black, fontSize = 15.sp),
            singleLine = true,
        )

        Spacer(modifier = Modifier.height(10.dp))

        Button(
            onClick = {
                if (TextUtils.isEmpty(name.value.toString())) {
                    Toast.makeText(context, "Please enter name", Toast.LENGTH_SHORT).show()
                } else if (TextUtils.isEmpty(age.value.toString())) {
                    Toast.makeText(context, "Please enter age", Toast.LENGTH_SHORT)
                        .show()
                } else if (TextUtils.isEmpty(address.value.toString())) {
                    Toast.makeText(context, "Please enter address", Toast.LENGTH_SHORT)
                        .show()
                } else {
                    addDataToFirebase(
                        name.value,
                        age.value,
                        address.value, context
                    )
                }
            },
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            // on below line we are adding text for our button
            Text(text = "Add Data", modifier = Modifier.padding(8.dp))
        }
        Spacer(modifier = Modifier.height(10.dp))
        Button(
            onClick = {
                navController.navigate("course")
            },
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            // on below line we are adding text for our button
            Text(text = "View Courses", modifier = Modifier.padding(8.dp))
        }
    }
}

fun addDataToFirebase(
    name: String,
    age: String,
    address: String,
    context: Context
) {
    val db: FirebaseFirestore = FirebaseFirestore.getInstance()
    val dbCourses: CollectionReference = db.collection("Courses")
    val courses = Course(name, age, address)
    dbCourses.add(courses).addOnSuccessListener {
        Toast.makeText(
            context,
            "Your Course has been added to Firebase Firestore",
            Toast.LENGTH_SHORT
        ).show()
    }.addOnFailureListener { e ->
        Toast.makeText(context, "Fail to add course \n$e", Toast.LENGTH_SHORT).show()
    }

}