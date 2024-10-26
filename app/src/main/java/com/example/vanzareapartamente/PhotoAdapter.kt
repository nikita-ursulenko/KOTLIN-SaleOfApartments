package com.example.vanzareapartamente

import android.net.Uri
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.recyclerview.widget.RecyclerView

class PhotoAdapter(
    private val photoUris: MutableList<Uri>,
    private val onPhotoRemoved: (Uri) -> Unit
) : RecyclerView.Adapter<PhotoAdapter.PhotoViewHolder>() {

    class PhotoViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val imageView: ImageView = itemView.findViewById(R.id.photoImageView)
        val deleteButton: ImageView = itemView.findViewById(R.id.deletePhotoButton)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PhotoViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_photo, parent, false)
        return PhotoViewHolder(view)
    }

    override fun onBindViewHolder(holder: PhotoViewHolder, position: Int) {
        val uri = photoUris[position]
        holder.imageView.setImageURI(uri)

        holder.deleteButton.setOnClickListener {
            onPhotoRemoved(uri) // Удаляем фото
        }
    }

    override fun getItemCount(): Int = photoUris.size
}