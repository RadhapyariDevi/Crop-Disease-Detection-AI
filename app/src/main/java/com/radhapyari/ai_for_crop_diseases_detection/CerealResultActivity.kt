package com.radhapyari.ai_for_crop_diseases_detection

import android.graphics.Bitmap
import android.media.ThumbnailUtils
import android.os.Bundle
import android.widget.ImageView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import android.net.Uri
import android.provider.MediaStore
import android.util.Log
import android.view.View
import android.widget.TextView
import android.widget.Toast
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.radhapyari.ai_for_crop_diseases_detection.ml.TfliteModelRice
import com.radhapyari.ai_for_crop_diseases_detection.ml.WheatTfliteModel
import org.tensorflow.lite.DataType
import org.tensorflow.lite.support.tensorbuffer.TensorBuffer
import java.io.InputStream
import java.nio.ByteBuffer
import java.nio.ByteOrder

class CerealResultActivity : AppCompatActivity() {

    private lateinit var imageView: ImageView
    private var imageSize = 128

    data class DiseaseInfo(
        val name: String,
        val cause: String,
        val prevention: List<String>
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_cereal_result)

        // Debug: Log the layout file
        Log.d("DEBUG", "Layout file: activity_cereal_result")

        // Initialize views with null checks
        val resultDiseaseView: TextView? = findViewById(R.id.disease_name_cereal)
        val resultCauseView: TextView? = findViewById(R.id.cause_context_cereal)
        val resultPreventionView: TextView? = findViewById(R.id.prevention_context_cereal)

        if (resultDiseaseView == null || resultCauseView == null || resultPreventionView == null) {
            Log.e("DEBUG", "One or more TextViews not found in layout")
            return
        }

        // Rest of your code
        val imageView: ImageView = findViewById(R.id.result_image_cereal)

        val imageUriString = intent.getStringExtra("image_uri")
        val cerealType = intent.getStringExtra("cereal_type") ?: "Unknown"

        var bitmap: Bitmap? = null
        if (imageUriString != null) {
            val imageUri = Uri.parse(imageUriString)
            imageView.setImageURI(imageUri)
            bitmap = MediaStore.Images.Media.getBitmap(contentResolver, imageUri)
        }

        val bottomSheet: View = findViewById(R.id.sheet_cereal)
        val bottomSheetBehavior = BottomSheetBehavior.from(bottomSheet)
        bottomSheetBehavior.peekHeight = 200
        bottomSheetBehavior.state = BottomSheetBehavior.STATE_HIDDEN

        if (bitmap != null) {
            val dimension = minOf(bitmap.width, bitmap.height)
            val thumbnail = ThumbnailUtils.extractThumbnail(bitmap, dimension, dimension)
            val resizedImage = Bitmap.createScaledBitmap(thumbnail, imageSize, imageSize, false)

            val resultIndex: Int

            if (cerealType.equals("wheat", ignoreCase = true)) {
                resultIndex = classifyWheat(resizedImage)

                val class_name = arrayOf(
                    "Wheat__Yellow_Rust",
                    "Wheat__Healthy",
                    "Wheat__Brown_Rust"
                )

                // Verify resultIndex
                if (resultIndex < 0 || resultIndex >= class_name.size) {
                    Log.e("DEBUG", "Invalid resultIndex: $resultIndex")
                } else {
                    val diseaseKey = class_name[resultIndex]
                    Log.d("DEBUG", "Disease Key: $diseaseKey")

                    // Load and parse JSON
                    val inputStream: InputStream = assets.open("wheat_diseases.json")
                    val jsonString = loadJSONFromAssets(inputStream)
                    Log.d("DEBUG", "JSON String: ${jsonString?.take(100)}")

                    val diseaseMap = parseDiseaseData(jsonString)
                    if (diseaseMap == null) {
                        Log.e("DEBUG", "Failed to parse disease data")
                    } else {
                        Log.d("DEBUG", "Disease Map: $diseaseMap")

                        // Get disease info
                        val diseaseInfo = diseaseMap[diseaseKey]
                        if (diseaseInfo == null) {
                            Log.e("DEBUG", "No disease info found for key: $diseaseKey")
                        } else {
                            Log.d("DEBUG", "Disease Info: ${diseaseInfo.name}, ${diseaseInfo.cause}, ${diseaseInfo.prevention}")

                            // Expand bottom sheet
                            bottomSheetBehavior.state = BottomSheetBehavior.STATE_EXPANDED
                            Log.d("DEBUG", "Bottom Sheet State: ${bottomSheetBehavior.state}")

                            // Update UI on main thread
                            runOnUiThread {
                                resultDiseaseView.text = diseaseInfo.name
                                resultCauseView.text = diseaseInfo.cause
                                resultPreventionView.text = diseaseInfo.prevention.joinToString("\n")
                            }
                        }
                    }
                }
            } else if (cerealType.equals("rice", ignoreCase = true)) {
                resultIndex = classifyRice(resizedImage)

                val class_name = arrayOf(
                    "leaf_scald",
                    "brown_spot",
                    "healthy",
                    "bacterial_leaf_blight",
                    "leaf_blast",
                    "narrow_brown_spot"
                )

                val inputStream: InputStream = assets.open("rice_diseases.json")
                val jsonString = loadJSONFromAssets(inputStream)

                Log.d("DEBUG", "JSON String Loaded: ${jsonString?.take(100)}")

                val diseaseMap = parseDiseaseData(jsonString)
                val diseaseKey = class_name[resultIndex]
                val diseaseInfo = diseaseMap?.get(diseaseKey)

                bottomSheetBehavior.state = BottomSheetBehavior.STATE_EXPANDED

                if (diseaseInfo != null) {
                    resultDiseaseView.text = diseaseInfo.name
                    resultCauseView.text = diseaseInfo.cause
                    resultPreventionView.text = diseaseInfo.prevention.joinToString("\n")
                }
            } else {
                resultDiseaseView.text = "Unknown cereal type"
            }
        }
    }





    private fun classifyWheat(image: Bitmap):Int{
        val model = WheatTfliteModel.newInstance(applicationContext)

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




    private fun classifyRice(image: Bitmap):Int{
        val model = TfliteModelRice.newInstance(applicationContext)

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



    private fun loadJSONFromAssets(inputStream: InputStream): String? {
        return try {
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