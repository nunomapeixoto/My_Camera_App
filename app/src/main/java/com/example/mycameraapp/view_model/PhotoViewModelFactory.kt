package com.example.mycameraapp.view_model

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.mycameraapp.model.PhotoModel

class PhotoViewModelFactory(private val photoModel: PhotoModel) : ViewModelProvider.NewInstanceFactory() {

    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>) = PhotoViewModel(
        photoModel
    ) as T


}