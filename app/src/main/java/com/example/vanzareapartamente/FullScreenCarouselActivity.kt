package com.example.vanzareapartamente

import android.net.Uri
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.viewpager2.widget.ViewPager2

class FullScreenCarouselActivity : AppCompatActivity() {

    private lateinit var viewPager: ViewPager2

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_fullscreen_carousel)

        viewPager = findViewById(R.id.photoViewPager)

        val imageUris = intent.getParcelableArrayListExtra<Uri>("imageUris") ?: arrayListOf()
        val startPosition = intent.getIntExtra("position", 0)

        val adapter = ImagePagerAdapter(imageUris) {}
        viewPager.adapter = adapter
        viewPager.setCurrentItem(startPosition, false)
    }
}