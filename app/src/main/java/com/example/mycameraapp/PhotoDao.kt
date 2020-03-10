package com.example.mycameraapp

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import io.reactivex.Flowable

@Dao
interface PhotoDao{

    @Query("SELECT * FROM photo")
    fun getPhotos(): Flowable<List<Photo>>

    @Insert
    fun inserPhoto(photo: Photo): Long

}