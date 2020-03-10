package com.example.mycameraapp

import androidx.lifecycle.ViewModel
import io.reactivex.Flowable

class PhotoViewModel internal constructor(private val photoModel: PhotoModel) : ViewModel() {

    fun getPhotos(): Flowable<List<Photo>> {
        return photoModel.getPhotos()
    }

    fun insertPhoto(photo: Photo) {
        photoModel.insertPhoto(photo)
    }

}