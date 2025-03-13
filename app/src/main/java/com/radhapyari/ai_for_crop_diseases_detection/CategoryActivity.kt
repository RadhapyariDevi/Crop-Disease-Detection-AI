package com.radhapyari.ai_for_crop_diseases_detection

import android.content.Intent
import android.os.Bundle
import android.widget.ImageView
import androidx.appcompat.app.AppCompatActivity

class CategoryActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_category)

        // Retrieve the previous key-value pair correctly
        val previousSelection = intent.getStringExtra("clickedImage") // "scanButton" or "uploadButton"

        // ImageView references
        val cereal = findViewById<ImageView>(R.id.cereal)
        val fruit = findViewById<ImageView>(R.id.fruit)

        // Set click listeners for each category image with unique activity redirection
        cereal.setOnClickListener {
            if (previousSelection == "scanButton") {
                startActivity(Intent(this, CerealScanActivity::class.java))
            } else {
                startActivity(Intent(this, CerealUploadActivity::class.java))
            }
        }

        fruit.setOnClickListener {
            if (previousSelection == "scanButton") {
                startActivity(Intent(this, FruitScanActivity::class.java))
            } else {
                startActivity(Intent(this, FruitUploadActivity::class.java))
            }
        }
    }
}