package com.example.projectdemo.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.projectdemo.model.Album
import com.example.projectdemo.model.AlbumDataProvider

class DatingViewModel:ViewModel(){
    private  val _albumLiveData = MutableLiveData<MutableList<Album>>()
    val albumLiveData: LiveData<MutableList<Album>> = _albumLiveData

    init {
        getAlbums()
    }

    private fun getAlbums() {
        _albumLiveData.value = AlbumDataProvider.albums.take(7).toMutableList()
    }
}