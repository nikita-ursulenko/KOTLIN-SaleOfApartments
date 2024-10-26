package com.example.vanzareapartamente

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.ImageButton
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView

class MainActivity : AppCompatActivity() {

    private lateinit var apartmentsRecyclerView: RecyclerView
    private lateinit var addApartmentButton: Button
    private lateinit var logoutButton: Button
    private lateinit var dbHelper: DatabaseHelper
    private lateinit var filterButton: ImageButton
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        // Находим кнопку фильтра
        filterButton = findViewById(R.id.filterButton)

        apartmentsRecyclerView = findViewById(R.id.apartmentsRecyclerView)
        addApartmentButton = findViewById(R.id.addApartmentButton)
        logoutButton = findViewById(R.id.logoutButton)

        dbHelper = DatabaseHelper(this)

        apartmentsRecyclerView.layoutManager = LinearLayoutManager(this)

        loadApartments()

        // Переход на AddEditApartmentActivity для добавления новой квартиры
        addApartmentButton.setOnClickListener {
            val intent = Intent(this, AddEditApartmentActivity::class.java)
            startActivity(intent)
        }

        // Логика выхода из системы
        logoutButton.setOnClickListener {
            saveLoginStatus(false)
            navigateToLoginActivity()
        }

        filterButton.setOnClickListener {
            showFilterDialog() // Отображаем диалог с фильтром
        }

    }
    override fun onResume() {
        super.onResume()
        loadApartments() // Загружаем список квартир при возврате на этот экран
    }
    private fun loadApartments(orderByAscending: Boolean = true) {
        // Загружаем квартиры с учетом сортировки
        val apartments = if (orderByAscending) {
            dbHelper.getAllApartmentsSortedAscending() // Сортировка по возрастанию
        } else {
            dbHelper.getAllApartmentsSortedDescending() // Сортировка по убыванию
        }

        // Логируем полученные квартиры для отладки
        apartments.forEach {
            Log.d("MainActivity", "Apartment: ${it.id}, Name: ${it.name}, Price: ${it.price}")
        }

        // Устанавливаем адаптер с передачей данных в ApartmentDetailActivity
        val adapter = ApartmentAdapter(apartments) { apartment ->
            Log.d("MainActivity", "Opening details for apartment ID: ${apartment.id}")

            // Переход к экрану с деталями квартиры
            val intent = Intent(this, ApartmentDetailActivity::class.java).apply {
                putExtra("apartmentId", apartment.id) // Передаем ID выбранной квартиры
            }
            startActivity(intent) // Запуск активности
        }

        apartmentsRecyclerView.adapter = adapter // Устанавливаем адаптер для RecyclerView
    }

    private fun saveLoginStatus(isLoggedIn: Boolean) {
        val sharedPreferences = getSharedPreferences("login_prefs", Context.MODE_PRIVATE)
        val editor = sharedPreferences.edit()
        editor.putBoolean("isLoggedIn", isLoggedIn)
        editor.apply()
    }

    private fun navigateToLoginActivity() {
        val intent = Intent(this, LoginActivity::class.java)
        startActivity(intent)
        finish()
    }

    // Показ диалога выбора фильтра
    private fun showFilterDialog() {
        val options = arrayOf("По возрастанию цены", "По убыванию цены")
        AlertDialog.Builder(this)
            .setTitle("Фильтр")
            .setItems(options) { _, which ->
                when (which) {
                    0 -> loadApartments(orderByAscending = true)
                    1 -> loadApartments(orderByAscending = false)
                }
            }
            .show()
    }

}