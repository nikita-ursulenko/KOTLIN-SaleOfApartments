package com.example.vanzareapartamente

import android.net.Uri
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import java.text.DecimalFormat
import java.text.DecimalFormatSymbols
import java.util.Locale

class ApartmentAdapter(
    private val apartments: List<Apartment>,
    private val onApartmentClick: (Apartment) -> Unit
) : RecyclerView.Adapter<ApartmentAdapter.ApartmentViewHolder>() {

    // ViewHolder для хранения ссылок на элементы списка
    class ApartmentViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val nameTextView: TextView = itemView.findViewById(R.id.apartmentName)
        val priceTextView: TextView = itemView.findViewById(R.id.apartmentPrice)
        val imageView: ImageView = itemView.findViewById(R.id.apartmentImage)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ApartmentViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_apartment, parent, false)
        return ApartmentViewHolder(view)
    }

    override fun onBindViewHolder(holder: ApartmentViewHolder, position: Int) {
        val apartment = apartments[position]

        // Устанавливаем название квартиры
        holder.nameTextView.text = apartment.name

        // Форматируем цену с разделением на тысячи
        holder.priceTextView.text = apartment.price?.let {
            "${formatPrice(it)} $"
        } ?: "Цена не указана"

        // Загрузка первого изображения из списка
        val imageUris = apartment.imageUri.split(",").filter { it.isNotEmpty() }
        if (imageUris.isNotEmpty()) {
            val uri = Uri.parse(imageUris[0])
            try {
                holder.imageView.setImageURI(uri)
            } catch (e: SecurityException) {
                Log.e("ApartmentAdapter", "Unable to load image: $uri", e)
                holder.imageView.setImageResource(R.drawable.ic_placeholder)
            }
        } else {
            holder.imageView.setImageResource(R.drawable.ic_placeholder)
        }

        // Обработка нажатия на элемент списка
        holder.itemView.setOnClickListener {
            onApartmentClick(apartment)
        }
    }

    // Форматирование числа с разделителями тысяч
    private fun formatPrice(price: Int): String {
        val formatter = DecimalFormat("#,###", DecimalFormatSymbols(Locale.US))
        return formatter.format(price)
    }

    override fun getItemCount(): Int = apartments.size
}