package com.example.projectdemo.model

import com.example.projectdemo.R

object AlbumDataProvider {
    val albums = mutableListOf(
        Album(
            id = 1 ,
            fruitName = "Apple",
            description = "Apple description",
            imageId = R.drawable.apple,
        ),
        Album(
            id = 2 ,
            fruitName = "Banana",
            description = "Banana description",
            imageId = R.drawable.banana,

        ),
        Album(
            id = 3 ,
            fruitName = "Cherries",
            description = "Cherries description",
            imageId = R.drawable.cherries
        ), Album(
            id = 4 ,
            fruitName = "Dates",
            description = "Dates description",
            imageId = R.drawable.dates
        ),
        Album(
            id = 5 ,
            fruitName = "EggFruit",
            description = "EggFruit description",
            imageId = R.drawable.eggfruit
        ),
        Album(
            id = 6 ,
            fruitName = "Fig",
            description = "Fig description",
            imageId = R.drawable.fig
        )
    )
}