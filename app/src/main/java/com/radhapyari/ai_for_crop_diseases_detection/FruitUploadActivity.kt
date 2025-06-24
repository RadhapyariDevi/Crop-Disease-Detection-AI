package com.radhapyari.ai_for_crop_diseases_detection

import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import android.content.Intent
import android.graphics.BitmapFactory
import android.net.Uri
import android.text.Html
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import com.radhapyari.ai_for_crop_diseases_detection.Constants.MODEL_PATH
import com.radhapyari.ai_for_crop_diseases_detection.FruitScanActivity
import com.radhapyari.ai_for_crop_diseases_detection.utils.LeafDetector


class FruitUploadActivity : AppCompatActivity() {

    private lateinit var leafdetector: LeafDetector

    private val pickImageLauncher = registerForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
        if (uri != null) {

            val inputStream = contentResolver.openInputStream(uri!!)
            val bitmap = BitmapFactory.decodeStream(inputStream)
            inputStream?.close()
            val leafFound = leafdetector.detectLeaf(bitmap)

            if(leafFound) {
                val intent = Intent(this, FruitResultActivity::class.java)
                intent.putExtra("image_uri", uri.toString())
                startActivity(intent)
                finish()
            }else{
                AlertDialog.Builder(this@FruitUploadActivity)
                    .setMessage(Html.fromHtml("<b>No leaf was found in the image.</b> \nPlease try again with a clear picture of a leaf."))
                    .setPositiveButton("OK") { dialog, _ ->
                        dialog.dismiss()
                        openGallery() // Reopen the gallery
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
        openGallery()
        enableEdgeToEdge()
        setContentView(R.layout.activity_fruit_upload)

        leafdetector = LeafDetector(this,MODEL_PATH)
        leafdetector.setUp()


    }
    private fun openGallery() {
        pickImageLauncher.launch("image/*")
    }


}