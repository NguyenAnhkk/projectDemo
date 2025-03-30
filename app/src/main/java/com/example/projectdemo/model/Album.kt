package com.example.projectdemo.model

import java.io.Serializable

data class Album(
    val id: Int,
    val fruitName:String,
    val description:String,
    val imageId: Int,
    val swiped: Boolean = false,
):Serializable
