package com.radhapyari.ai_for_crop_diseases_detection

import android.content.Intent
import android.os.Bundle
import android.widget.ImageView
import androidx.appcompat.app.AppCompatActivity

class HomeScreenActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_home_screen)

        // Hide action bar for a cleaner UI
        supportActionBar?.hide()

        // Find the clickable images
        val scanButton = findViewById<ImageView>(R.id.scanButton)
        val uploadButton = findViewById<ImageView>(R.id.uploadButton)
        val chatbotButton = findViewById<ImageView>(R.id.chatbotButton)

        // OnClickListener for Scan Button
        scanButton.setOnClickListener {
            val intent = Intent(this, CategoryActivity::class.java)
            intent.putExtra("clickedImage", "scanButton")  // Remember Selection
            startActivity(intent)
        }

        // OnClickListener for Upload Button
        uploadButton.setOnClickListener {
            val intent = Intent(this, CategoryActivity::class.java)
            intent.putExtra("clickedImage", "uploadButton")  // Remember Selection
            startActivity(intent)
        }

        // No action for Chatbot Button (Vacant for now)
        chatbotButton.setOnClickListener {
            // No action yet
        }
    }
}