package com.example.mycameraapp

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity
data class Photo (
    val name: String,
    val date: String,
    @PrimaryKey
    val uri: String
)