package com.radhapyari.ai_for_crop_diseases_detection

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.ImageView
import android.widget.Spinner
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity

class CategoryActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_category)


        val previousSelection = intent.getStringExtra("clickedImage") // "scanButton" or "uploadButton"


        val fruit = findViewById<ImageView>(R.id.fruit)
        val cerealDropdown = findViewById<Spinner>(R.id.cerealDropdown)


        val cerealOptions = arrayOf("Select Type", "RICE", "WHEAT")
        val adapter = ArrayAdapter(this, android.R.layout.simple_spinner_dropdown_item, cerealOptions)
        cerealDropdown.adapter = adapter


        cerealDropdown.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>, view: View?, position: Int, id: Long) {
                val selectedCereal = cerealOptions[position]

                // Prevent "Select Type" from triggering an intent
                if (selectedCereal != "Select Type") {
                    val intent = if (previousSelection == "scanButton") {
                        Intent(this@CategoryActivity, CerealScanActivity::class.java)
                    } else {
                        Intent(this@CategoryActivity, CerealUploadActivity::class.java)
                    }
                    intent.putExtra("cerealType", selectedCereal)
                    startActivity(intent)
                }
            }

            override fun onNothingSelected(parent: AdapterView<*>) {}
        }


        fruit.setOnClickListener {
            val intent = if (previousSelection == "scanButton") {
                Intent(this, FruitScanActivity::class.java)
            } else {
                Intent(this, FruitUploadActivity::class.java)
            }
            startActivity(intent)
        }
    }
}
