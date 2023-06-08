package com.example.slowmotionapp.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class SharedViewModel : ViewModel() {
    private val _videoPath = MutableLiveData<String>()
    val videoPath: LiveData<String> = _videoPath

    val booleanLiveData: MutableLiveData<Boolean> = MutableLiveData()

    val booleanCropVisible: MutableLiveData<Boolean> = MutableLiveData()

    val cropSelected: MutableLiveData<Int> = MutableLiveData()

    val startCrop: MutableLiveData<Boolean> = MutableLiveData()

    val fragmentA: MutableLiveData<Boolean> = MutableLiveData()

    fun setVideoUri(path: String) {
        _videoPath.value = path
    }

    fun pauseVideo(newValue: Boolean) {
        booleanLiveData.value = newValue
    }

    fun cropViewVisible(newValue: Boolean) {
        booleanCropVisible.value = newValue
    }

    fun cropSelected(newValue: Int) {
        cropSelected.value = newValue
    }

    fun startCrop(newValue: Boolean) {
        startCrop.value = newValue
    }

    fun switchFragmentB(newValue: Boolean) {
        fragmentA.value = newValue
    }

}
