package com.radhapyari.ai_for_crop_diseases_detection

import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import android.content.Intent
import android.net.Uri
import android.provider.MediaStore
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts


class ProduceUploadActivity : AppCompatActivity() {
    private val pickImageLauncher = registerForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
        if (uri != null) {
            val intent = Intent(this, ProduceResultActivity::class.java)
            intent.putExtra("image_uri", uri.toString())
            startActivity(intent)
            finish()
        } else {
            Toast.makeText(this, "No Image Selected", Toast.LENGTH_SHORT).show()
            finish()
        }
    }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        openGallery()
        enableEdgeToEdge()
        setContentView(R.layout.activity_fruit_upload)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
    }
    private fun openGallery() {
        pickImageLauncher.launch("image/*")
    }


}