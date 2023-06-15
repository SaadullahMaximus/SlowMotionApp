package com.example.slowmotionapp.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.slowmotionapp.models.Mp3Store
import com.example.slowmotionapp.repository.Mp3StoreRepository

class Mp3StoreViewModel(private val repository: Mp3StoreRepository) : ViewModel() {
    private val _mp3Stores = MutableLiveData<List<Mp3Store>>()
    val mp3Stores: LiveData<List<Mp3Store>> = _mp3Stores

    private val _error = MutableLiveData<String>()
    val error: LiveData<String> = _error

    fun fetchMp3Stores() {
        repository.getMp3Stores { mp3StoresList, exception ->
            if (exception != null) {
                _error.value = "Error fetching mp3 stores"
            } else {
                _mp3Stores.value = mp3StoresList
            }
        }
    }
}
