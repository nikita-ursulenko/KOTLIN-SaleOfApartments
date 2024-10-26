package com.example.vanzareapartamente

import android.content.Intent
import android.graphics.Typeface
import android.net.Uri
import android.os.Bundle
import android.text.SpannableString
import android.text.Spanned
import android.text.style.StyleSpan
import android.util.Log
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.viewpager2.widget.ViewPager2
import android.app.AlertDialog
import java.text.DecimalFormat
import java.text.DecimalFormatSymbols
import java.util.Locale

class ApartmentDetailActivity : AppCompatActivity() {
    private var apartmentId: Int = -1
    private lateinit var viewPager: ViewPager2 // ViewPager2 для карусели
    private lateinit var nameTextView: TextView
    private lateinit var locationTextView: TextView
    private lateinit var priceTextView: TextView
    private lateinit var editButton: Button
    private lateinit var deleteButton: Button
    private lateinit var dbHelper: DatabaseHelper
    private lateinit var photoCounter: TextView  // Счётчик фотографий
    private lateinit var imagePagerAdapter: ImagePagerAdapter
    private lateinit var cityTextView: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_apartment_detail) // Устанавливаем макет

        cityTextView = findViewById(R.id.cityTextView)
        // Инициализируем компоненты интерфейса
        viewPager = findViewById(R.id.imageViewPager)
        nameTextView = findViewById(R.id.apartmentNameTextView)
        locationTextView = findViewById(R.id.apartmentLocationTextView)
        priceTextView = findViewById(R.id.apartmentPriceTextView)
        editButton = findViewById(R.id.editApartmentButton)
        deleteButton = findViewById(R.id.deleteApartmentButton)

        dbHelper = DatabaseHelper(this) // Инициализируем базу данных

        photoCounter = findViewById(R.id.photoCounter)
        val imageUris = intent.getParcelableArrayListExtra<Uri>("imageUris") ?: arrayListOf()
        setupViewPager(imageUris)
        updatePhotoCounter(0, imageUris.size)


        // Получаем ID квартиры из Intent
        apartmentId = intent.getIntExtra("apartmentId", -1)
        Log.d("ApartmentDetail", "Received apartmentId: $apartmentId")

        if (apartmentId != -1) {
            loadApartmentDetails(apartmentId) // Загружаем детали квартиры
        } else {
            Log.e("ApartmentDetail", "Invalid apartment ID")
            Toast.makeText(this, "Ошибка загрузки данных", Toast.LENGTH_SHORT).show()
            finish()
        }


        // В onCreate замените код удаления на вызов диалога:
        deleteButton.setOnClickListener {
            showDeleteConfirmationDialog() // Показ диалогового окна перед удалением
        }

        // Новый Intent для перехода к редактированию объявления
        editButton.setOnClickListener {
            val intent = Intent(this, AddEditApartmentActivity::class.java)
            intent.putExtra("apartmentId", apartmentId) // Передаем ID для редактирования
            startActivity(intent)
        }

    }

    /**
     * Функция для применения жирного стиля к ключевым словам в строке.
     * @param fullText Полный текст.
     * @param boldPart Часть текста, которая должна быть выделена жирным.
     * @return SpannableString с примененными стилями.
     */
    private fun applyBoldStyle(fullText: String, boldPart: String): SpannableString {
        val spannable = SpannableString(fullText)
        val startIndex = fullText.indexOf(boldPart)
        if (startIndex != -1) {
            spannable.setSpan(
                StyleSpan(Typeface.BOLD), // Жирный стиль
                startIndex, startIndex + boldPart.length, // Индексы слова для форматирования
                Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
            )
        }
        return spannable
    }
    // Код удаления квартиры с подтверждением
    private fun showDeleteConfirmationDialog() {
        // Создаем диалоговое окно
        val builder = AlertDialog.Builder(this)
        builder.setTitle("Подтверждение удаления") // Устанавливаем заголовок
        builder.setMessage("Вы уверены, что хотите удалить эту квартиру?") // Текст сообщения

        // Кнопка подтверждения
        builder.setPositiveButton("Удалить") { _, _ ->
            dbHelper.deleteApartment(apartmentId) // Удаляем квартиру из БД
            Toast.makeText(this, "Квартира удалена", Toast.LENGTH_SHORT).show()
            finish() // Закрываем активность после удаления
        }

        // Кнопка отмены
        builder.setNegativeButton("Отмена") { dialog, _ ->
            dialog.dismiss() // Закрываем диалог без удаления
        }

        // Отображаем диалог
        builder.create().show()
    }
    // Загружаем информацию о квартире
    private fun loadApartmentDetails(id: Int) {
        val apartment = dbHelper.getApartmentById(id)
        Log.d("ApartmentDetail", "Loaded apartment: $apartment")

        if (apartment != null) {
            // Устанавливаем данные квартиры
            // Устанавливаем название квартиры
            nameTextView.text = apartment.name
            // Отображаем город
            val CityText = "Город: ${apartment.city}"
            cityTextView.text = applyBoldStyle(CityText, "Город")
            // Формируем и устанавливаем текст для локации с выделенным словом "Район"
            val locationText = "Район: ${apartment.location}"
            locationTextView.text = applyBoldStyle(locationText, "Район")



            // Создаем список выбранных параметров
            val features = mutableListOf<String>()
            if (apartment.hasFurniture) features.add("С мебелью")
            if (apartment.isNewBuilding) features.add("Новостройка")
            if (apartment.allowsPets) features.add("Разрешены животные")
            if (apartment.hasParking) features.add("Парковка")
            if (apartment.hasBalcony) features.add("Балкон/Терраса")

            // Проверяем, есть ли выбранные опции
            if (features.isNotEmpty()) {
                // Создаем строку с заголовком "Опции:\n" и списком опций
                val featuresText = "Опции:\n" + features.joinToString(separator = "\n") { "- $it" }

                // Применяем жирный стиль только к слову "Опции:"
                val styledFeaturesText = applyBoldStyle(featuresText, "Опции:")

                // Устанавливаем текст в TextView
                findViewById<TextView>(R.id.featuresTextView).text = styledFeaturesText
            } else {
                // Если опций нет, очищаем TextView
                findViewById<TextView>(R.id.featuresTextView).text = ""
            }

            // Форматируем цену и применяем стиль
            val formattedPrice = formatPrice(apartment.price)

            // Передаем в строку ресурс два аргумента: "Цена" и форматированную цену
            val priceText = getString(R.string.apartment_price, "Цена", formattedPrice)

            // Устанавливаем отформатированный текст с применением жирного стиля
            priceTextView.text = applyBoldStyle(priceText, "Цена")


            // Загружаем список URI изображений
            val imageUris = apartment.imageUri.split(",").map { Uri.parse(it) }

            if (imageUris.isNotEmpty()) {
                setupViewPager(imageUris) // Настраиваем карусель
            } else {
                Toast.makeText(this, "Нет изображений для отображения", Toast.LENGTH_SHORT).show()
            }
        } else {
            Toast.makeText(this, "Квартира не найдена", Toast.LENGTH_SHORT).show()
            finish() // Закрываем активность, если данные не найдены
        }
    }
    private fun formatPrice(price: Int): String {
        val formatter = DecimalFormat("#,###", DecimalFormatSymbols(Locale.US))
        return formatter.format(price)
    }
    // Настраиваем ViewPager2 с адаптером
    private fun setupViewPager(imageUris: List<Uri>) {
        // Создаем адаптер с обработчиком кликов
        imagePagerAdapter = ImagePagerAdapter(imageUris) { position ->
            // Открываем полноэкранную карусель при нажатии на изображение
            val intent = Intent(this, FullScreenCarouselActivity::class.java).apply {
                putParcelableArrayListExtra("imageUris", ArrayList(imageUris))
                putExtra("position", position)
            }
            startActivity(intent)
        }

        viewPager.adapter = imagePagerAdapter // Устанавливаем адаптер для ViewPager

        // Обрабатываем смену страницы в ViewPager
        viewPager.registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback() {
            override fun onPageSelected(position: Int) {
                super.onPageSelected(position)
                // Обновляем счетчик фотографий при смене страницы
                updatePhotoCounter(position, imageUris.size)
            }
        })
    }
    private fun updatePhotoCounter(currentPosition: Int, total: Int) {
        val counterText = "${currentPosition + 1} / $total"
        photoCounter.text = counterText
    }
    override fun onResume() {
        super.onResume()
        loadApartmentDetails(apartmentId) // Загружаем список квартир при возврате на этот экран
    }
}