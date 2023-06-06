package com.example.slowmotionapp.viewmodel

import android.net.Uri
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class SharedViewModel : ViewModel() {
    private val _videoUri = MutableLiveData<Uri>()
    val videoUri: LiveData<Uri> = _videoUri

    fun setVideoUri(uri: Uri) {
        _videoUri.value = uri
    }
}
