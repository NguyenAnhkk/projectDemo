package com.example.projectdemo.viewdata

import android.util.Log
import android.view.View
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.FirebaseFirestoreException
import com.google.firebase.firestore.ktx.toObject
import com.google.firebase.firestore.toObject
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

class DataViewModel:ViewModel(){
    val state = mutableStateOf<List<Course>>(emptyList())

    init {
        getData()
    }
    private fun getData(){
        viewModelScope.launch { state.value = getDataFromFireStore() }
    }
}
suspend fun getDataFromFireStore(): List<Course> {
    val db = FirebaseFirestore.getInstance()
    val courses = mutableListOf<Course>()
    try {
        val snapshot = db.collection("Courses").get().await()
        courses.addAll(snapshot.documents.mapNotNull { it.toObject(Course::class.java) })
    } catch (e: FirebaseFirestoreException) {
        Log.d("error", "getDataFromFireStore: $e")
    }
    return courses
}