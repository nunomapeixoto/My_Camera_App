package com.example.mycameraapp.view_model

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.mycameraapp.model.FirebaseModel

class FirebaseViewModelFactory(private val firebaseModel: FirebaseModel) : ViewModelProvider.NewInstanceFactory() {


    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>) = FirebaseViewModel(
        firebaseModel
    ) as T


}