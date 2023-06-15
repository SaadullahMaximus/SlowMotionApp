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

    val musicSet: MutableLiveData<Boolean> = MutableLiveData()

    val audioVolumeLevel: MutableLiveData<Float> = MutableLiveData()

    val videoVolumeLevel: MutableLiveData<Float> = MutableLiveData()

    val enhanced: MutableLiveData<Boolean> = MutableLiveData()

    val downloadedMusic: MutableLiveData<String> = MutableLiveData()


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

    fun musicSetCheck(newValue: Boolean) {
        musicSet.value = newValue
    }

    fun audioVolumeLevelCheck(newValue: Float) {
        audioVolumeLevel.value = newValue
    }

    fun videoVolumeLevelCheck(newValue: Float) {
        videoVolumeLevel.value = newValue
    }

    fun enhanced(newValue: Boolean) {
        enhanced.value = newValue
    }

    fun downloadMusicPath(newValue: String) {
        downloadedMusic.value = newValue
    }

}
