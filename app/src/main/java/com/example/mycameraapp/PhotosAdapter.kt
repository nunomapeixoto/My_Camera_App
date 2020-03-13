package com.example.mycameraapp

import android.net.Uri
import android.os.Build
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.google.firebase.storage.FirebaseStorage

class PhotosAdapter(private val photosList: List<Photo>, private val photoAdapterListener: PhotoAdapterListener) :
    RecyclerView.Adapter<PhotosAdapter.MyViewHolder>() {

    val storage = FirebaseStorage.getInstance()

    interface PhotoAdapterListener {
        fun onPhotoClick(photoUri: String)
    }

    class MyViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

        val nameTv: TextView = itemView.findViewById(R.id.name_tv)
        val dateTv: TextView = itemView.findViewById(R.id.date_tv)
        val photoIv: ImageView = itemView.findViewById(R.id.thumbnail_iv)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyViewHolder {
        val itemView = LayoutInflater.from(parent.context).inflate(R.layout.photo_item, parent, false)
        return MyViewHolder(itemView)
    }

    override fun getItemCount(): Int {
        return photosList.size
    }

    override fun onBindViewHolder(holder: MyViewHolder, position: Int) {
        val photo = photosList[position]

        holder.dateTv.text = photo.date
        holder.nameTv.text = photo.name
//        Glide.with(holder.itemView).load(Uri.parse(photo.uri)).into(holder.photoIv)
        GlideApp.with(holder.itemView).load(storage.getReference(photo.uri)).into(holder.photoIv)

        holder.itemView.setOnClickListener { photoAdapterListener.onPhotoClick(photo.uri) }
    }
}