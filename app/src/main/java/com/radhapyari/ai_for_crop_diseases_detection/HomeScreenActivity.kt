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


        val scanButton = findViewById<ImageView>(R.id.scanButton)
        val uploadButton = findViewById<ImageView>(R.id.uploadButton)
        val chatbotButton = findViewById<ImageView>(R.id.chatbotButton)


        scanButton.setOnClickListener {
            val intent = Intent(this, CategoryActivity::class.java)
            intent.putExtra("clickedImage", "scanButton")  // Remember Selection
            startActivity(intent)
        }


        uploadButton.setOnClickListener {
            val intent = Intent(this, CategoryActivity::class.java)
            intent.putExtra("clickedImage", "uploadButton")  // Remember Selection
            startActivity(intent)
        }


        chatbotButton.setOnClickListener {
            val intent = Intent(this, ChatbotActivity::class.java)
            startActivity(intent)
        }
    }
}