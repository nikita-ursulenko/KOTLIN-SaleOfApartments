package com.example.vanzareapartamente

import android.content.ContentValues
import android.content.Context
import android.database.Cursor
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper

class DatabaseHelper(context: Context) : SQLiteOpenHelper(context, "apartments.db", null, 1) {

    override fun onCreate(db: SQLiteDatabase) {
        // Создаем таблицы для пользователей и квартир
        db.execSQL("CREATE TABLE users (id INTEGER PRIMARY KEY AUTOINCREMENT, email TEXT UNIQUE, password TEXT)")
        db.execSQL(
            "CREATE TABLE apartments (" +
                    "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                    "name TEXT, " +
                    "city TEXT," +
                    "location TEXT, " +
                    "price INTEGER, " +
                    "imageUris TEXT," +
                    "hasFurniture INTEGER, " +  // 1 - да, 0 - нет
                    "isNewBuilding INTEGER, " + // 1 - новострой, 0 - нет
                    "allowsPets INTEGER, " +    // 1 - разрешены животные, 0 - нет
                    "hasParking INTEGER, " +    // 1 - есть парковка, 0 - нет
                    "hasBalcony INTEGER)"
        )

    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        db.execSQL("DROP TABLE IF EXISTS users")
        db.execSQL("DROP TABLE IF EXISTS apartments")
        onCreate(db)
    }

    // Добавление нового пользователя (Create)
    fun insertUser(email: String, password: String): Boolean {
        val db = writableDatabase
        val values = ContentValues().apply {
            put("email", email)
            put("password", password)
        }
        val result = db.insert("users", null, values)
        return result != -1L
    }

    // Проверка пользователя (Read)
    fun checkUser(email: String, password: String): Boolean {
        val db = readableDatabase
        val cursor = db.rawQuery(
            "SELECT * FROM users WHERE email = ? AND password = ?",
            arrayOf(email, password)
        )
        val exists = cursor.count > 0
        cursor.close()
        return exists
    }

    // Получение всех квартир (Read)
    fun getAllApartments(): List<Apartment> {
        val apartments = mutableListOf<Apartment>()
        val db = readableDatabase
        val cursor = db.rawQuery("SELECT * FROM apartments", null)
        if (cursor.moveToFirst()) {
            do {
                val id = cursor.getInt(cursor.getColumnIndexOrThrow("id"))
                val name = cursor.getString(cursor.getColumnIndexOrThrow("name"))
                val city = cursor.getString(cursor.getColumnIndexOrThrow("city"))
                val location = cursor.getString(cursor.getColumnIndexOrThrow("location"))
                val price = cursor.getInt(cursor.getColumnIndexOrThrow("price"))
                val imageUris = cursor.getString(cursor.getColumnIndexOrThrow("imageUris"))
                val hasFurniture = cursor.getInt(cursor.getColumnIndexOrThrow("hasFurniture")) == 1
                val isNewBuilding = cursor.getInt(cursor.getColumnIndexOrThrow("isNewBuilding")) == 1
                val allowsPets = cursor.getInt(cursor.getColumnIndexOrThrow("allowsPets")) == 1
                val hasParking = cursor.getInt(cursor.getColumnIndexOrThrow("hasParking")) == 1
                val hasBalcony = cursor.getInt(cursor.getColumnIndexOrThrow("hasBalcony")) == 1
                Apartment(id, name, city, location, price, imageUris, hasFurniture, isNewBuilding, allowsPets, hasParking, hasBalcony)
            } while (cursor.moveToNext())
        }
        cursor.close()
        return apartments
    }

    fun insertApartment(
        name: String, city: String, location: String, price: Int, imageUris: String,
        withFurniture: Boolean, newBuilding: Boolean, petsAllowed: Boolean,
        parkingAvailable: Boolean, hasBalcony: Boolean
    ): Long {
        val db = writableDatabase
        val values = ContentValues().apply {
            put("name", name)
            put("city", city)
            put("location", location)
            put("price", price)
            put("imageUris", imageUris)
            put("hasFurniture", if (withFurniture) 1 else 0)
            put("isNewBuilding", if (newBuilding) 1 else 0)
            put("allowsPets", if (petsAllowed) 1 else 0)
            put("hasParking", if (parkingAvailable) 1 else 0)
            put("hasBalcony", if (hasBalcony) 1 else 0)
        }
        return db.insert("apartments", null, values)
    }

    // Обновление информации о квартире (Update)
    fun updateApartment(
        id: Int, name: String, city: String, location: String, price: Int, imageUris: String,
        withFurniture: Boolean, newBuilding: Boolean, petsAllowed: Boolean,
        parkingAvailable: Boolean, hasBalcony: Boolean
    ): Int {
        val db = writableDatabase
        val values = ContentValues().apply {
            put("name", name)
            put("city", city)
            put("location", location)
            put("price", price)
            put("imageUris", imageUris)
            put("hasFurniture", if (withFurniture) 1 else 0)
            put("isNewBuilding", if (newBuilding) 1 else 0)
            put("allowsPets", if (petsAllowed) 1 else 0)
            put("hasParking", if (parkingAvailable) 1 else 0)
            put("hasBalcony", if (hasBalcony) 1 else 0)
        }
        return db.update("apartments", values, "id = ?", arrayOf(id.toString()))
    }

    // Удаление квартиры (Delete)
    fun deleteApartment(id: Int): Int {
        val db = writableDatabase
        return db.delete("apartments", "id = ?", arrayOf(id.toString()))
    }

    // Получение одной квартиры по ID (для деталей или редактирования)
    fun getApartmentById(id: Int): Apartment? {
        val db = readableDatabase
        val cursor = db.rawQuery("SELECT * FROM apartments WHERE id = ?", arrayOf(id.toString()))

        return if (cursor.moveToFirst()) {
            val name = cursor.getString(cursor.getColumnIndexOrThrow("name"))
            val city = cursor.getString(cursor.getColumnIndexOrThrow("city"))
            val location = cursor.getString(cursor.getColumnIndexOrThrow("location"))
            val price = cursor.getInt(cursor.getColumnIndexOrThrow("price"))
            val imageUris = cursor.getString(cursor.getColumnIndexOrThrow("imageUris"))
            val hasFurniture = cursor.getInt(cursor.getColumnIndexOrThrow("hasFurniture")) == 1
            val isNewBuilding = cursor.getInt(cursor.getColumnIndexOrThrow("isNewBuilding")) == 1
            val allowsPets = cursor.getInt(cursor.getColumnIndexOrThrow("allowsPets")) == 1
            val hasParking = cursor.getInt(cursor.getColumnIndexOrThrow("hasParking")) == 1
            val hasBalcony = cursor.getInt(cursor.getColumnIndexOrThrow("hasBalcony")) == 1

            cursor.close()
            Apartment(id, name, city, location, price, imageUris, hasFurniture, isNewBuilding, allowsPets, hasParking, hasBalcony)
        } else {
            cursor.close()
            null
        }
    }
    fun getAllApartmentsSortedAscending(): List<Apartment> {
        val apartments = mutableListOf<Apartment>()
        val db = readableDatabase
        val cursor = db.rawQuery("SELECT * FROM apartments ORDER BY price ASC", null)
        if (cursor.moveToFirst()) {
            do {
                apartments.add(extractApartmentFromCursor(cursor))
            } while (cursor.moveToNext())
        }
        cursor.close()
        return apartments
    }

    fun getAllApartmentsSortedDescending(): List<Apartment> {
        val apartments = mutableListOf<Apartment>()
        val db = readableDatabase
        val cursor = db.rawQuery("SELECT * FROM apartments ORDER BY price DESC", null)
        if (cursor.moveToFirst()) {
            do {
                apartments.add(extractApartmentFromCursor(cursor))
            } while (cursor.moveToNext())
        }
        cursor.close()
        return apartments
    }

    private fun extractApartmentFromCursor(cursor: Cursor): Apartment {
        val id = cursor.getInt(cursor.getColumnIndexOrThrow("id"))
        val name = cursor.getString(cursor.getColumnIndexOrThrow("name"))
        val city = cursor.getString(cursor.getColumnIndexOrThrow("city"))
        val location = cursor.getString(cursor.getColumnIndexOrThrow("location"))
        val price = cursor.getInt(cursor.getColumnIndexOrThrow("price"))
        val imageUris = cursor.getString(cursor.getColumnIndexOrThrow("imageUris"))
        // Извлечение значений для чекбоксов (1 = true, 0 = false)
        val hasFurniture = cursor.getInt(cursor.getColumnIndexOrThrow("hasFurniture")) == 1
        val isNewBuilding = cursor.getInt(cursor.getColumnIndexOrThrow("isNewBuilding")) == 1
        val allowsPets = cursor.getInt(cursor.getColumnIndexOrThrow("allowsPets")) == 1
        val hasParking = cursor.getInt(cursor.getColumnIndexOrThrow("hasParking")) == 1
        val hasBalcony = cursor.getInt(cursor.getColumnIndexOrThrow("hasBalcony")) == 1

        return Apartment(id, name, city, location, price, imageUris, hasFurniture, isNewBuilding, allowsPets, hasParking, hasBalcony)
    }
}