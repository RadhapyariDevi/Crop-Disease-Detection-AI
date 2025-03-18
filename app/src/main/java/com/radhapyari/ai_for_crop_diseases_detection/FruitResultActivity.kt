package com.radhapyari.ai_for_crop_diseases_detection

import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import android.Manifest
import android.graphics.Bitmap
import android.media.ThumbnailUtils
import android.provider.MediaStore
import android.widget.Button
import android.widget.TextView
import androidx.activity.result.contract.ActivityResultContracts
import com.radhapyari.ai_for_crop_diseases_detection.ml.TfliteModel
import org.tensorflow.lite.DataType
import org.tensorflow.lite.support.tensorbuffer.TensorBuffer
import java.io.IOException
import java.nio.ByteBuffer
import java.nio.ByteOrder

import android.net.Uri
import android.util.Log
import android.view.View
import android.widget.ImageView
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import java.io.InputStream


class FruitResultActivity : AppCompatActivity() {

    private lateinit var imageView: ImageView
    private lateinit var result: TextView
    private var imageSize = 128

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

            val resultIndex = ClassifyImage(resizedImage)

            val class_name = arrayOf(
                "Apple_scab",
                "Bacterial_spot",
                "Black_rot",
                "Cedar_apple_rust",
                "Cercospora_leaf_spot Gray_leaf_spot",
                "Common_rust",
                "Early_blight",
                "Esca_(Black_Measles)",
                "Haunglongbing_(Citrus_greening)",
                "Late_blight",
                "Leaf_Mold",
                "Leaf_blight_(Isariopsis_Leaf_Spot)",
                "Leaf_scorch",
                "Northern_Leaf_Blight",
                "Septoria_leaf_spot",
                "Spider_mites Two-spotted_spider_mite",
                "Target_Spot",
                "Tomato_Yellow_Leaf_Curl_Virus",
                "Tomato_mosaic_virus",
                "healthy"
            )


            val jsonString = loadJSONFromAssets()
            Log.d("DEBUG", "JSON String Loaded: ${jsonString?.take(100)}") // Print first 100 chars

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
    private fun ClassifyImage(image: Bitmap):Int{
        val model = TfliteModel.newInstance(applicationContext)

        val byteBuffer = ByteBuffer.allocateDirect(4 * 128 * 128 * 3)
        byteBuffer.order(ByteOrder.nativeOrder())


        val intValues = IntArray(imageSize * imageSize)
        image.getPixels(intValues, 0, image.width, 0, 0, image.width, image.height)

        var pixel = 0
        for (i in 0 until imageSize) {
            for (j in 0 until imageSize) {
                val value = intValues[pixel++]
                byteBuffer.putFloat(((value shr 16) and 0xFF) / 255.0f)
                byteBuffer.putFloat(((value shr 8) and 0xFF) / 255.0f)
                byteBuffer.putFloat((value and 0xFF) / 255.0f)
            }
        }


        val inputFeature0 = TensorBuffer.createFixedSize(intArrayOf(1, 128, 128, 3), DataType.FLOAT32)
        inputFeature0.loadBuffer(byteBuffer)


        val outputs = model.process(inputFeature0)
        val outputFeature0 = outputs.outputFeature0AsTensorBuffer
        val outputArray = outputFeature0.floatArray
        val resultIndex = outputArray.indices.maxByOrNull { outputArray[it] } ?: -1

        model.close()
        return resultIndex
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


