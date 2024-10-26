    package com.example.vanzareapartamente

    import android.net.Uri
    import android.view.LayoutInflater
    import android.view.View
    import android.view.ViewGroup
    import androidx.recyclerview.widget.RecyclerView
    import io.getstream.photoview.PhotoView

    class ImagePagerAdapter(
        private val imageUris: List<Uri>,
        private val onItemClick: (Int) -> Unit // Передаем позицию по клику
    ) : RecyclerView.Adapter<ImagePagerAdapter.ImageViewHolder>() {

        inner class ImageViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
            private val photoView: PhotoView = itemView.findViewById(R.id.photoView)

            fun bind(uri: Uri) {
                photoView.setImageURI(uri)
                photoView.setOnClickListener {
                    onItemClick(adapterPosition) // Передаем позицию при клике
                }
            }
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ImageViewHolder {
            val view = LayoutInflater.from(parent.context)
                .inflate(R.layout.item_photo_view, parent, false)
            return ImageViewHolder(view)
        }

        override fun onBindViewHolder(holder: ImageViewHolder, position: Int) {
            holder.bind(imageUris[position])
        }

        override fun getItemCount(): Int = imageUris.size
    }