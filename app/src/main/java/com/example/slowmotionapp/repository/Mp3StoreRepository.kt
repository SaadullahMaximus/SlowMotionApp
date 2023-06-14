package com.example.slowmotionapp.repository

import com.example.slowmotionapp.models.Mp3Store
import com.google.firebase.firestore.FirebaseFirestore

class Mp3StoreRepository {
    private val firebaseFireStore: FirebaseFirestore = FirebaseFirestore.getInstance()
    private val collectionName = "/Music"

    fun getMp3Stores(callback: (List<Mp3Store>?, Exception?) -> Unit) {
        firebaseFireStore.collection(collectionName)
            .get()
            .addOnSuccessListener { querySnapshot ->
                val mp3Stores = mutableListOf<Mp3Store>()

                for (document in querySnapshot) {
                    val link = document.getString("link")
                    val name = document.getString("name")

                    if (link != null && name != null) {
                        val mp3Store = Mp3Store(link, name)
                        mp3Stores.add(mp3Store)
                    }
                }

                callback(mp3Stores, null)
            }
            .addOnFailureListener { exception ->
                callback(null, exception)
            }
    }
}
