package com.example.mycameraapp

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider

class FirebaseViewModelFactory(private val firebaseModel: FirebaseModel) : ViewModelProvider.NewInstanceFactory() {


    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>) = FirebaseViewModel(firebaseModel) as T


}