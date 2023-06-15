package com.example.slowmotionapp.viewmodelfactory

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.slowmotionapp.repository.Mp3StoreRepository
import com.example.slowmotionapp.viewmodel.Mp3StoreViewModel

class Mp3StoreViewModelFactory(private val repository: Mp3StoreRepository) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(Mp3StoreViewModel::class.java)) {
            return Mp3StoreViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}

