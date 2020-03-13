package com.example.mycameraapp.view_model

import androidx.lifecycle.ViewModel
import com.example.mycameraapp.Photo
import com.example.mycameraapp.model.PhotoModel
import io.reactivex.Flowable

class PhotoViewModel internal constructor(private val photoModel: PhotoModel) : ViewModel() {

    fun getPhotos(): Flowable<List<Photo>> {
        return photoModel.getPhotos()
    }

    fun insertPhoto(photo: Photo) {
        photoModel.insertPhoto(photo)
    }

}