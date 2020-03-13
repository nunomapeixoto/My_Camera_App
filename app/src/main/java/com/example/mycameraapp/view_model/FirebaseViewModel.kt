package com.example.mycameraapp.view_model

import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModel
import com.example.mycameraapp.model.FirebaseModel
import com.example.mycameraapp.Photo
import io.reactivex.Observable

class FirebaseViewModel(private val firebaseModel: FirebaseModel) : ViewModel() {

    fun login(user: String, pwd: String, activity: AppCompatActivity) {
          firebaseModel.login(user, pwd, activity)
    }

    fun getLoginResult(): Observable<Int> {
        return firebaseModel.authResults
    }

    fun register(user: String, pwd: String, activity: AppCompatActivity) {
        firebaseModel.register(user, pwd, activity)
    }

    fun uploadPhoto(photo: Photo) {
        firebaseModel.uploadPhoto(photo)
    }

    fun getPhotosList() {
        firebaseModel.listPhotos()
    }

    fun getPhotos(): Observable<List<Photo>> {
        return firebaseModel.photosObservableList
    }

    fun getNewUploadedPhoto(): Observable<Photo> {
        return firebaseModel.photoObservable
    }

    fun getUploadProgress(): Observable<Int>{
        return firebaseModel.uploadProgressObservable
    }

    fun getStorageResults(): Observable<Int> {
        return firebaseModel.storageResults
    }
}