package com.radhapyari.ai_for_crop_diseases_detection

import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import android.Manifest
import android.graphics.Bitmap
import android.graphics.Typeface
import android.media.ThumbnailUtils
import android.provider.MediaStore
import android.widget.Button
import android.widget.TextView
import androidx.activity.result.contract.ActivityResultContracts
import org.tensorflow.lite.DataType
import org.tensorflow.lite.support.tensorbuffer.TensorBuffer
import java.io.IOException
import java.nio.ByteBuffer
import java.nio.ByteOrder

import android.net.Uri
import android.util.Log
import android.util.TypedValue
import android.view.View
import android.widget.ImageView
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.radhapyari.ai_for_crop_diseases_detection.ml.TfliteMobilenetModel
import java.io.InputStream


class FruitResultActivity : AppCompatActivity() {

    private lateinit var imageView: ImageView
    private lateinit var result: TextView
    private var imageSize = 224

    data class DiseaseInfo(
        val name: String,
        val cause: String,
        val prevention: List<String>
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_fruit_result)

        val imageView: ImageView = findViewById(R.id.result_image)

        val imageUriString = intent.getStringExtra("image_uri")

        var bitmap: Bitmap? = null

        if (imageUriString != null) {
            val imageUri = Uri.parse(imageUriString)

            imageView.setImageURI(imageUri)

            bitmap = MediaStore.Images.Media.getBitmap(contentResolver, imageUri)


        }

        val bottomSheet: View = findViewById(R.id.sheet)
        val bottomSheetBehavior = BottomSheetBehavior.from(bottomSheet)
        bottomSheetBehavior.peekHeight = 200
        bottomSheetBehavior.state = BottomSheetBehavior.STATE_HIDDEN
//        val resultTextView1: TextView = findViewById(R.id.disease_name)
//        resultTextView1.text = "Please Wait..."


        if (bitmap != null) {
            val dimension = minOf(bitmap.width, bitmap.height)
            val thumbnail = ThumbnailUtils.extractThumbnail(bitmap, dimension, dimension)

            val resizedImage = Bitmap.createScaledBitmap(thumbnail, imageSize, imageSize, false)

            val (resultIndex, confidence) = ClassifyImage(resizedImage)

            val class_name = arrayOf(
                "Apple___Apple_scab",                      // 0
                "Apple___Black_rot",                       // 1
                "Apple___Cedar_apple_rust",                // 2
                "Apple___healthy",                         // 3
                "Blueberry___healthy",                     // 4
                "Cherry_(including_sour)___Powdery_mildew",// 5
                "Cherry_(including_sour)___healthy",       // 6
                "Corn_(maize)___Cercospora_leaf_spot Gray_leaf_spot", // 7
                "Corn_(maize)___Common_rust_",             // 8
                "Corn_(maize)___Northern_Leaf_Blight",     // 9
                "Corn_(maize)___healthy",                  // 10
                "Grape___Black_rot",                       // 11
                "Grape___Esca_(Black_Measles)",            // 12
                "Grape___Leaf_blight_(Isariopsis_Leaf_Spot)", // 13
                "Grape___healthy",                         // 14
                "Orange___Haunglongbing_(Citrus_greening)",// 15
                "Peach___Bacterial_spot",                  // 16
                "Peach___healthy",                         // 17
                "Pepper,_bell___Bacterial_spot",           // 18
                "Pepper,_bell___healthy",                  // 19
                "Potato___Early_blight",                   // 20
                "Potato___Late_blight",                    // 21
                "Potato___healthy",                        // 22
                "Raspberry___healthy",                     // 23
                "Soybean___healthy",                       // 24
                "Squash___Powdery_mildew",                 // 25
                "Strawberry___Leaf_scorch",                // 26
                "Strawberry___healthy",                    // 27
                "Tomato___Bacterial_spot",                 // 28
                "Tomato___Early_blight",                   // 29
                "Tomato___Late_blight",                    // 30
                "Tomato___Leaf_Mold",                      // 31
                "Tomato___Septoria_leaf_spot",             // 32
                "Tomato___Spider_mites Two-spotted_spider_mite", // 33
                "Tomato___Target_Spot",                    // 34
                "Tomato___Tomato_Yellow_Leaf_Curl_Virus",  // 35
                "Tomato___Tomato_mosaic_virus",            // 36
                "Tomato___healthy"                         // 37
            )
            val confidenceThreshold = 0.7f
            Log.d("DEBUG", "Model Confidence: $confidence")

            if (confidence < confidenceThreshold) {
                val resultDiseaseView: TextView = findViewById(R.id.disease_name)
                resultDiseaseView.text = "Sorry, I couldn’t confidently identify the disease from this image.\n" +
                        " I perform best on crops such as apple, grape, tomato, potato, corn, peach, strawberry, pepper and similar commonly supported plants."
                resultDiseaseView.setTypeface(null, Typeface.NORMAL)
                resultDiseaseView.setTextSize(TypedValue.COMPLEX_UNIT_SP, 20f)
                val resultCauseView: TextView = findViewById(R.id.cause_context)
                resultCauseView.text = "No data found"

                val resultPreventionView: TextView = findViewById(R.id.prevention_context)
                resultPreventionView.text = "No data found"

                bottomSheetBehavior.state = BottomSheetBehavior.STATE_EXPANDED
                return
            }


            val jsonString = loadJSONFromAssets()

            val diseaseMap = parseDiseaseData(jsonString)

            if (diseaseMap != null) {
                Log.d("DEBUG", "Disease Map Loaded Successfully, Size: ${diseaseMap.size}")
            } else {
                Log.e("DEBUG", "Disease Map is NULL")
            }

            val diseaseKey = class_name[resultIndex]
            Log.d("DEBUG", "Predicted Disease Key: $diseaseKey")

            val diseaseInfo = diseaseMap?.get(diseaseKey)

            if (diseaseInfo != null) {
                Log.d("DEBUG", "Disease Info Retrieved: ${diseaseInfo.name}")
            } else {
                Log.e("DEBUG", "No data found for key: $diseaseKey")
            }




            bottomSheetBehavior.state = BottomSheetBehavior.STATE_EXPANDED

            if (diseaseInfo != null) {
                val resultDiseaseView: TextView = findViewById(R.id.disease_name)
                resultDiseaseView.text = diseaseInfo.name

                val resultCauseView: TextView = findViewById(R.id.cause_context)
                resultCauseView.text = diseaseInfo.cause

                val resultPreventionView: TextView = findViewById(R.id.prevention_context)
                resultPreventionView.text = diseaseInfo.prevention.joinToString("\n")
            }

        }


    }
    private fun ClassifyImage(image: Bitmap): Pair<Int, Float> {
        val model = TfliteMobilenetModel.newInstance(applicationContext)


        val byteBuffer = ByteBuffer.allocateDirect(4 * 224 * 224 * 3)
        byteBuffer.order(ByteOrder.nativeOrder())


        val intValues = IntArray(imageSize * imageSize)
        image.getPixels(intValues, 0, image.width, 0, 0, image.width, image.height)

        var pixel = 0
        for (i in 0 until imageSize) {
            for (j in 0 until imageSize) {
                val value = intValues[pixel++]
                val r = ((value shr 16) and 0xFF) / 127.5f - 1.0f
                val g = ((value shr 8) and 0xFF) / 127.5f - 1.0f
                val b = (value and 0xFF) / 127.5f - 1.0f

                byteBuffer.putFloat(r)
                byteBuffer.putFloat(g)
                byteBuffer.putFloat(b)
            }
        }


        val inputFeature0 = TensorBuffer.createFixedSize(intArrayOf(1, 224, 224, 3), DataType.FLOAT32)
        inputFeature0.loadBuffer(byteBuffer)


        val outputs = model.process(inputFeature0)
        val outputFeature0 = outputs.outputFeature0AsTensorBuffer
        val outputArray = outputFeature0.floatArray
        val resultIndex = outputArray.indices.maxByOrNull { outputArray[it] } ?: -1
        val confidence = outputArray[resultIndex]

        model.close()
        return Pair(resultIndex, confidence)
    }


    private fun loadJSONFromAssets(): String? {
        return try {
            val inputStream: InputStream = assets.open("crop_diseases.json")
            val size = inputStream.available()
            val buffer = ByteArray(size)
            inputStream.read(buffer)
            inputStream.close()
            String(buffer, Charsets.UTF_8)
        } catch (e: Exception) {
            Log.e("DEBUG", "LoadJson error: ${e.message}")
            null
        }
    }



    private fun parseDiseaseData(jsonString: String?): Map<String, DiseaseInfo>? {
        if (jsonString == null) return null

        return try {
            val gson = Gson()
            val type = object : TypeToken<Map<String, DiseaseInfo>>() {}.type
            gson.fromJson(jsonString, type)
        } catch (e: Exception) {
            Log.e("DEBUG", "JSON Parsing error: ${e.message}")
            null
        }
    }
}


