package com.example.mycameraapp

import android.content.Context

object Injection {

    private fun getPhotoModel(context: Context): PhotoModel {
        return PhotoModel.getInstance(
                AppDatabase.getInstance(context.applicationContext).photoDao())
    }

    fun providePhotoViewModelFactory(context: Context): PhotoViewModelFactory {
        val model = getPhotoModel(context)
        return PhotoViewModelFactory(model)
    }

}