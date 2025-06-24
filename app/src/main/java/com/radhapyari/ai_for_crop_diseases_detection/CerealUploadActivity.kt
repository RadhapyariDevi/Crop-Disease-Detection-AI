package com.radhapyari.ai_for_crop_diseases_detection

import android.content.Intent
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Bundle
import android.text.Html
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.radhapyari.ai_for_crop_diseases_detection.Constants.MODEL_PATH
import com.radhapyari.ai_for_crop_diseases_detection.FruitScanActivity
import com.radhapyari.ai_for_crop_diseases_detection.FruitUploadActivity
import com.radhapyari.ai_for_crop_diseases_detection.utils.LeafDetector

class CerealUploadActivity : AppCompatActivity() {

    private lateinit var leafdetector: LeafDetector

    // Initialize pickImageLauncher
    private val pickImageLauncher = registerForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
        if (uri != null) {

            val selectedCereal = intent.getStringExtra("cerealType") ?: "Unknown"

            val inputStream = contentResolver.openInputStream(uri!!)
            val bitmap = BitmapFactory.decodeStream(inputStream)
            inputStream?.close()
            val leafFound = leafdetector.detectLeaf(bitmap)

            if(leafFound) {
                val intent = Intent(this, CerealResultActivity::class.java)
                intent.putExtra("image_uri", uri.toString())
                intent.putExtra("cereal_type", selectedCereal)
                startActivity(intent)
                finish()
            }else{
                AlertDialog.Builder(this@CerealUploadActivity)
                    .setMessage(Html.fromHtml("<b>No leaf was found in the image.</b> \nPlease try again with a clear picture of a leaf."))
                    .setPositiveButton("OK") { dialog, _ ->
                        dialog.dismiss()
                        openGallery()
                    }
                    .setCancelable(false)
                    .show()
            }
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

        leafdetector = LeafDetector(this,MODEL_PATH)
        leafdetector.setUp()
    }

    private fun openGallery() {
        pickImageLauncher.launch("image/*")
    }
}