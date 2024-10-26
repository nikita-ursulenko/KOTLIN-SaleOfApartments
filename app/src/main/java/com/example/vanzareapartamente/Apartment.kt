package com.example.vanzareapartamente

data class Apartment(
    val id: Int,
    val name: String,
    val city: String,
    val location: String,
    val price: Int,
    val imageUri: String,
    val hasFurniture: Boolean,
    val isNewBuilding: Boolean,
    val allowsPets: Boolean,
    val hasParking: Boolean,
    val hasBalcony: Boolean
)