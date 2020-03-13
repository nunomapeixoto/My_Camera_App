package com.example.mycameraapp.model

import com.example.mycameraapp.Photo
import com.example.mycameraapp.PhotoDao
import io.reactivex.Completable
import io.reactivex.Flowable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers

class PhotoModel private constructor(private val photoDao: PhotoDao) {

    fun getPhotos(): Flowable<List<Photo>> {
        return photoDao.getPhotos()
    }

    fun insertPhoto(photo: Photo) {
        Completable.fromAction { photoDao.inserPhoto(photo) }
            .subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread()).subscribe()

    }

    companion object {

        @Volatile
        private var instance: PhotoModel? = null

        fun getInstance(photoDao: PhotoDao) =
            instance ?: synchronized(this) {
                instance
                    ?: PhotoModel(photoDao).also { instance = it }
            }
    }

}