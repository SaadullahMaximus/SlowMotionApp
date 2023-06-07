package com.example.slowmotionapp.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class SharedViewModel : ViewModel() {
    private val _videoPath = MutableLiveData<String>()
    val videoPath: LiveData<String> = _videoPath

    val booleanLiveData: MutableLiveData<Boolean> = MutableLiveData()

    fun setVideoUri(path: String) {
        _videoPath.value = path
    }

    fun pauseVideo(newValue: Boolean) {
        booleanLiveData.value = newValue
    }

}
