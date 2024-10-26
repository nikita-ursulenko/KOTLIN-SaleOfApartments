package com.example.vanzareapartamente


import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.widget.ArrayAdapter
import android.widget.AutoCompleteTextView
import android.widget.Button
import android.widget.CheckBox
import android.widget.EditText
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView

import android.text.Editable
import android.text.TextWatcher
import java.text.DecimalFormat
import java.text.DecimalFormatSymbols
import java.util.Calendar
import java.util.Locale

class AddEditApartmentActivity : AppCompatActivity() {
    private var apartmentId: Int? = null
    private lateinit var nameInput: EditText
    private lateinit var locationInput: EditText
    private lateinit var cityInput: AutoCompleteTextView
    private lateinit var priceInput: EditText
    private lateinit var saveButton: Button
    private lateinit var selectPhotosButton: Button
    private lateinit var photosRecyclerView: RecyclerView
    private lateinit var dbHelper: DatabaseHelper

    private val selectedPhotoUris = mutableListOf<Uri>()
    private lateinit var photoAdapter: PhotoAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_add_edit_apartment)
        Log.d("AddEditApartment", "Активность запущена.")

        // Проверяем и устанавливаем таймер
        checkAndScheduleReminder()
        dbHelper = DatabaseHelper(this)
        nameInput = findViewById(R.id.apartmentNameInput)
        locationInput = findViewById(R.id.apartmentLocationInput)
        cityInput = findViewById(R.id.cityInput)
        priceInput = findViewById(R.id.apartmentPriceInput)

        priceInput.addTextChangedListener(object : TextWatcher {
            private var currentText = ""  // Сохраняем текущее состояние текста, чтобы избежать повторных обновлений

            override fun afterTextChanged(s: Editable?) {
                if (s.toString() == currentText) return  // Если текст не изменился, выходим

                s?.let {
                    // Убираем все точки и запятые из текста
                    val cleanString = s.toString().replace("[,.]".toRegex(), "")

                    // Преобразуем в число (если возможно)
                    val parsed = cleanString.toLongOrNull() ?: return  // Выходим, если не удалось преобразовать

                    // Форматируем число с запятыми каждые 3 цифры
                    val formatted = DecimalFormat("#,###", DecimalFormatSymbols(Locale.US)).format(parsed)

                    currentText = formatted  // Обновляем текущее состояние текста
                    priceInput.removeTextChangedListener(this)  // Удаляем слушатель, чтобы избежать рекурсии
                    priceInput.setText(formatted)  // Устанавливаем отформатированный текст
                    priceInput.setSelection(formatted.length)  // Ставим курсор в конец
                    priceInput.addTextChangedListener(this)  // Восстанавливаем слушатель
                }
            }

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
                // Нет необходимости в реализации
            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                // Нет необходимости в реализации
            }
        })

        saveButton = findViewById(R.id.saveApartmentButton)
        selectPhotosButton = findViewById(R.id.selectPhotosButton)
        photosRecyclerView = findViewById(R.id.photosRecyclerView)


        photoAdapter = PhotoAdapter(selectedPhotoUris) { uri ->
            removePhoto(uri)
        }
        photosRecyclerView.layoutManager = LinearLayoutManager(this, RecyclerView.HORIZONTAL, false)
        photosRecyclerView.adapter = photoAdapter

        // Получаем ID квартиры из Intent
        apartmentId = intent.getIntExtra("apartmentId", -1).takeIf { it != -1 }

        apartmentId?.let {
            Log.d("AddEditApartment", "Режим редактирования. ID: $apartmentId")
            loadApartmentDetails(it)  // Загружаем данные для редактирования
        }

        // Настройка списка городов
        val cities = listOf("Кишинев", "Бельцы", "Оргеев", "Кагул")
        val adapter = ArrayAdapter(this, android.R.layout.simple_dropdown_item_1line, cities)

        cityInput = findViewById(R.id.cityInput)
        cityInput.setAdapter(adapter)

        // Открываем список городов при нажатии
        cityInput.setOnClickListener {
            Log.d("CityInput", "Dropdown clicked")
            cityInput.showDropDown()  // Принудительное отображение списка
        }

        // Обрабатываем выбор города
        cityInput.setOnItemClickListener { parent, _, position, _ ->
            val selectedCity = parent.getItemAtPosition(position).toString()
            Toast.makeText(this, "Вы выбрали: $selectedCity", Toast.LENGTH_SHORT).show()
            Log.d("CityInput", "Selected city: $selectedCity")
        }

        val selectPhotosLauncher = registerForActivityResult(
            ActivityResultContracts.OpenMultipleDocuments()
        ) { uris ->
            Log.d("AddEditApartment", "Selected URIs: $uris")
            if (uris.isNotEmpty()) {
                uris.forEach { uri ->
                    grantUriPermission(uri)
                    selectedPhotoUris.add(uri)
                }
                photoAdapter.notifyDataSetChanged()
            }
        }

        selectPhotosButton.setOnClickListener {
            selectPhotosLauncher.launch(arrayOf("image/*"))
        }

        saveButton.setOnClickListener {
            saveOrUpdateApartment()
        }
    }

    private fun grantUriPermission(uri: Uri) {
        try {
            contentResolver.takePersistableUriPermission(
                uri,
                Intent.FLAG_GRANT_READ_URI_PERMISSION or Intent.FLAG_GRANT_WRITE_URI_PERMISSION
            )
            Log.d("AddEditApartment", "Granted permission for URI: $uri")
        } catch (e: Exception) {
            Log.e("AddEditApartment", "Error granting URI permission", e)
        }
    }
    private fun addPhotos(uris: List<Uri>) {
        val startPosition = selectedPhotoUris.size
        selectedPhotoUris.addAll(uris)
        photoAdapter.notifyItemRangeInserted(startPosition, uris.size) // Точное уведомление
    }
    private fun removePhoto(uri: Uri) {
        selectedPhotoUris.remove(uri)
        Log.d("AddEditApartment", "Photo removed: $uri")
        photoAdapter.notifyDataSetChanged()
    }
    private fun loadApartmentDetails(id: Int) {
        val apartment = dbHelper.getApartmentById(id)
        apartment?.let {
            findViewById<EditText>(R.id.apartmentNameInput).setText(apartment.name)
            findViewById<EditText>(R.id.apartmentLocationInput).setText(apartment.location)
            findViewById<AutoCompleteTextView>(R.id.cityInput).setText(apartment.city, false)
            findViewById<EditText>(R.id.apartmentPriceInput).setText(apartment.price.toString())
            // Устанавливаем состояния чекбоксов
            findViewById<CheckBox>(R.id.withFurnitureCheckBox).isChecked = it.hasFurniture
            findViewById<CheckBox>(R.id.newBuildingCheckBox).isChecked = it.isNewBuilding
            findViewById<CheckBox>(R.id.petsAllowedCheckBox).isChecked = it.allowsPets
            findViewById<CheckBox>(R.id.parkingCheckBox).isChecked = it.hasParking
            findViewById<CheckBox>(R.id.balconyCheckBox).isChecked = it.hasBalcony
            val uris = it.imageUri.split(",").map { uri -> Uri.parse(uri) }
            selectedPhotoUris.addAll(uris)
            photoAdapter.notifyDataSetChanged()
        }
    }
    private fun saveOrUpdateApartment() {
        val name = nameInput.text.toString()
        val city = cityInput.text.toString() // Получаем город
        val location = locationInput.text.toString()
        // Убираем точки из цены перед сохранением
        val cleanPriceString = priceInput.text.toString().replace("[,.]".toRegex(), "")
        val price = cleanPriceString.toIntOrNull()
        val imageUris = selectedPhotoUris.joinToString(",") { it.toString() }
        val withFurniture = findViewById<CheckBox>(R.id.withFurnitureCheckBox).isChecked
        val newBuilding = findViewById<CheckBox>(R.id.newBuildingCheckBox).isChecked
        val petsAllowed = findViewById<CheckBox>(R.id.petsAllowedCheckBox).isChecked
        val parkingAvailable = findViewById<CheckBox>(R.id.parkingCheckBox).isChecked
        val hasBalcony = findViewById<CheckBox>(R.id.balconyCheckBox).isChecked

        Log.d("AddEditApartment", "Saving apartment with URIs: $imageUris")
        if (name.isNotEmpty() && location.isNotEmpty() && city.isNotEmpty() && price != null) {
            if (selectedPhotoUris.isNotEmpty()) {
                val dbHelper = DatabaseHelper(this)
                apartmentId?.let {
                    cancelReminder()
                    dbHelper.updateApartment(it, name, city, location, price, imageUris, withFurniture, newBuilding, petsAllowed, parkingAvailable,hasBalcony)
                    Toast.makeText(this, "Объявление обновлено", Toast.LENGTH_SHORT).show()
                } ?: run {
                    cancelReminder()
                dbHelper.insertApartment(name, city, location, price, imageUris, withFurniture, newBuilding, petsAllowed, parkingAvailable,hasBalcony)
                Toast.makeText(this, "Объявление добавлено", Toast.LENGTH_SHORT).show()
                }
                finish()
            } else {
                Toast.makeText(this, "Добавьте хотя бы одно фото", Toast.LENGTH_SHORT).show()
            }
        } else {
            Toast.makeText(this, "Заполните все поля", Toast.LENGTH_SHORT).show()
        }
    }

    private fun scheduleReminder() {
        val alarmManager = getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val intent = Intent(this, ReminderReceiver::class.java)
        val pendingIntent = PendingIntent.getBroadcast(
            this, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val triggerTime = System.currentTimeMillis() + 10 * 1000 // 10 секунд

        try {
            alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, triggerTime, pendingIntent)
            Log.d("AddEditApartment", "Напоминание установлено через 10 секунд.")
        } catch (e: SecurityException) {
            Log.e("AddEditApartment", "Ошибка: нет разрешения для точных будильников.", e)
        }
    }
    private fun cancelReminder() {
        val alarmManager = getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val intent = Intent(this, ReminderReceiver::class.java)
        val pendingIntent = PendingIntent.getBroadcast(
            this, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        alarmManager.cancel(pendingIntent)
        Log.d("AddEditApartment", "Напоминание отменено.")
    }
    private fun checkAndScheduleReminder() {
        val alarmManager = getSystemService(Context.ALARM_SERVICE) as AlarmManager

        if (if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                alarmManager.canScheduleExactAlarms()
            } else {
                TODO("VERSION.SDK_INT < S")
            }
        ) {
            // Разрешение есть – устанавливаем таймер
            Log.d("AddEditApartment", "Разрешение есть – устанавливаем таймер.")
            scheduleReminder()
        } else {
            Log.d("AddEditApartment", "Разрешение есть – устанавливаем таймер.")
            // Разрешения нет – показываем объяснение или выполняем альтернативные действия
            requestExactAlarmPermission()
        }
    }
    private fun requestExactAlarmPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            val intent = Intent(android.provider.Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM).apply {
                data = Uri.fromParts("package", packageName, null)
            }
            startActivity(intent)  // Переходим в настройки для выдачи разрешения
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        cancelReminder() // Отключаем таймер при закрытии экрана
    }

}