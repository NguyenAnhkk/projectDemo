package com.example.projectdemo.viewdata

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CourseDetailsActivity(
    navController: NavController,
    dataViewModel: DataViewModel = viewModel()

) {
    val getData = dataViewModel.state.value
    var courseList = mutableStateListOf<Course?>()
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        LazyColumn {
            items(getData) { course ->
                ElevatedCard(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(8.dp),
                    elevation = CardDefaults.cardElevation(defaultElevation = 6.dp),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFFF1EDE9)),
                    onClick = {
                        navController.navigate("update_data/${course.Name}/${course.Age}/${course.Address}")
                    }
                ) {
                    Text(
                        text = "Name: ${course.Name}", color = Color(0xFF117a46), style = TextStyle(
                            fontSize = 20.sp
                        ), modifier = Modifier.padding(5.dp)
                    )
                    Text(
                        text = "Age: ${course.Age}", color = Color.Black, style = TextStyle(
                            fontSize = 15.sp
                        ), modifier = Modifier.padding(5.dp)
                    )
                    Text(
                        text = "Address: ${course.Address}", color = Color.Black, style = TextStyle(
                            fontSize = 15.sp
                        ), modifier = Modifier.padding(5.dp)
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                }

            }
        }


    }
}

