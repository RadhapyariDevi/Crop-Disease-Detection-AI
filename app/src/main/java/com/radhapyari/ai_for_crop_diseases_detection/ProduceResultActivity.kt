package com.radhapyari.ai_for_crop_diseases_detection

import android.graphics.Bitmap
import android.media.ThumbnailUtils
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.radhapyari.ai_for_crop_diseases_detection.databinding.ActivityProduceResultBinding
import com.radhapyari.ai_for_crop_diseases_detection.ml.Produce
import com.radhapyari.ai_for_crop_diseases_detection.ml.TfliteModel
import org.tensorflow.lite.DataType
import org.tensorflow.lite.support.tensorbuffer.TensorBuffer
import java.io.InputStream
import java.nio.ByteBuffer
import java.nio.ByteOrder

class ProduceResultActivity : AppCompatActivity() {

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

        val binding = ActivityProduceResultBinding.inflate(layoutInflater)
        setContentView(binding.root)


        val imageView: ImageView = findViewById(R.id.result_image)

        val imageUriString = intent.getStringExtra("image_uri")

        var bitmap: Bitmap? = null

        if (imageUriString != null) {
            val imageUri = Uri.parse(imageUriString)

            imageView.setImageURI(imageUri)

            bitmap = MediaStore.Images.Media.getBitmap(contentResolver, imageUri)


        }


        val bottomSheet: View = findViewById(R.id.sheet_produce)
        val bottomSheetBehavior = BottomSheetBehavior.from(bottomSheet)
        bottomSheetBehavior.peekHeight = 200
        bottomSheetBehavior.state = BottomSheetBehavior.STATE_HIDDEN


        if (bitmap != null) {
            val dimension = minOf(bitmap.width, bitmap.height)
            val thumbnail = ThumbnailUtils.extractThumbnail(bitmap, dimension, dimension)

            val resizedImage = Bitmap.createScaledBitmap(thumbnail, imageSize, imageSize, false)

            val resultIndex = ClassifyImage(resizedImage)

            val class_name = arrayOf(
                "Anthracnose_rot_produce",
                "black_rot_produce",
                "botrytis_rot_produce",
                "healthy"
            )

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
                val resultDiseaseView: TextView = findViewById(R.id.disease_name_produce)
                resultDiseaseView.text = diseaseInfo.name

                val resultCauseView: TextView = findViewById(R.id.cause_context_produce)
                resultCauseView.text = diseaseInfo.cause

                val resultPreventionView: TextView = findViewById(R.id.prevention_context_produce)
                resultPreventionView.text = diseaseInfo.prevention.joinToString("\n")
            }


        }

    }

    private fun ClassifyImage(image: Bitmap): Int {
        val model = Produce.newInstance(applicationContext)

        // Resize the image to 224x224
        val resizedImage = Bitmap.createScaledBitmap(image, imageSize, imageSize, false)

        // Create a ByteBuffer to hold the image data
        val byteBuffer = ByteBuffer.allocateDirect(4 * imageSize * imageSize * 3) // 4 bytes per float, 224x224x3
        byteBuffer.order(ByteOrder.nativeOrder())

        // Get pixel values and normalize them to [-1, 1] (for MobileNet)
        val intValues = IntArray(imageSize * imageSize)
        resizedImage.getPixels(intValues, 0, imageSize, 0, 0, imageSize, imageSize)

        var pixel = 0
        for (i in 0 until imageSize) {
            for (j in 0 until imageSize) {
                val value = intValues[pixel++]
                // Normalize pixel values to [-1, 1] (for MobileNet)
                byteBuffer.putFloat(((value shr 16) and 0xFF) / 127.5f - 1.0f) // Red
                byteBuffer.putFloat(((value shr 8) and 0xFF) / 127.5f - 1.0f)  // Green
                byteBuffer.putFloat((value and 0xFF) / 127.5f - 1.0f)          // Blue
            }
        }

        // Create the input tensor
        val inputFeature0 = TensorBuffer.createFixedSize(intArrayOf(1, imageSize, imageSize, 3), DataType.FLOAT32)
        inputFeature0.loadBuffer(byteBuffer)

        // Run model inference
        val outputs = model.process(inputFeature0)
        val outputFeature0 = outputs.outputFeature0AsTensorBuffer
        val outputArray = outputFeature0.floatArray

        // Get the index of the highest confidence class
        val resultIndex = outputArray.indices.maxByOrNull { outputArray[it] } ?: -1

        // Close the model
        model.close()

        return resultIndex
    }

    private fun loadJSONFromAssets(): String? {
        return try {
            val inputStream: InputStream = assets.open("crop_diseases_produce.json")
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



    private fun parseDiseaseData(jsonString: String?): Map<String, FruitResultActivity.DiseaseInfo>? {
        if (jsonString == null) return null

        return try {
            val gson = Gson()
            val type = object : TypeToken<Map<String, FruitResultActivity.DiseaseInfo>>() {}.type
            gson.fromJson(jsonString, type)
        } catch (e: Exception) {
            Log.e("DEBUG", "JSON Parsing error: ${e.message}")
            null
        }
    }


}