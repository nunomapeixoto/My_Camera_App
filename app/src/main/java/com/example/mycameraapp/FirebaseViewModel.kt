package com.example.mycameraapp

import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModel
import io.reactivex.Observable

class FirebaseViewModel(private val firebaseModel: FirebaseModel) : ViewModel() {

    fun login(user: String, pwd: String, activity: AppCompatActivity) {
          firebaseModel.login(user, pwd, activity)
    }

    fun getLoginResult(): Observable<Int> {
        return firebaseModel.authResultCode
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

}