package com.radhapyari.ai_for_crop_diseases_detection

import android.content.Intent
import android.os.Bundle
import android.widget.ImageView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import org.tensorflow.lite.support.label.Category

class firstCategoryActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_first_category)

        val previousSelection = intent.getStringExtra("clickedImage")

        val LeavesButton = findViewById<ImageView>(R.id.leaves)
        val ProduceButton = findViewById<ImageView>(R.id.produce)
        val chatbotButton = findViewById<ImageView>(R.id.chatbotButton)

        LeavesButton.setOnClickListener {
            val intent = if (previousSelection == "scanButton") {
                Intent(this, CategoryActivity::class.java).apply {
                    putExtra("clickedImage", "scanButton")
                }
            } else {
                Intent(this, FruitUploadActivity::class.java).apply {
                    putExtra("clickedImage", "uploadButton")
                }
            }
            startActivity(intent)
        }

        ProduceButton.setOnClickListener {
            val intent = if (previousSelection == "scanButton") {
                Intent(this, ProduceScanActivity::class.java).apply {
                    putExtra("clickedImage", "scanButton")
                }
            } else {
                Intent(this, ProduceUploadActivity::class.java).apply {
                    putExtra("clickedImage", "uploadButton")
                }
            }
            startActivity(intent)
        }



    }
}