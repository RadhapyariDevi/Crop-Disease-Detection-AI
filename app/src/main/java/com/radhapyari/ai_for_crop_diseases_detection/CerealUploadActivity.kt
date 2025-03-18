package com.radhapyari.ai_for_crop_diseases_detection

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity

class CerealUploadActivity : AppCompatActivity() {

    // Initialize pickImageLauncher
    private val pickImageLauncher = registerForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
        if (uri != null) {

            val selectedCereal = intent.getStringExtra("cerealType") ?: "Unknown"

            val intent = Intent(this, CerealResultActivity::class.java)
            intent.putExtra("image_uri", uri.toString())
            intent.putExtra("cereal_type", selectedCereal)
            startActivity(intent)
            finish()
        } else {
            Toast.makeText(this, "No Image Selected", Toast.LENGTH_SHORT).show()
            finish()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_cereal_upload)

        // Open the gallery
        openGallery()
    }

    private fun openGallery() {
        pickImageLauncher.launch("image/*")
    }
}