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
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.rememberBottomSheetScaffoldState
import androidx.compose.material3.rememberModalBottomSheetState
import android.net.Uri
import android.view.View
import android.widget.ImageView
import com.google.android.material.bottomsheet.BottomSheetBehavior


class FruitResultActivity : AppCompatActivity() {

    private lateinit var imageView: ImageView
    private lateinit var result: TextView
    private var imageSize = 128

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
        bottomSheetBehavior.state = BottomSheetBehavior.STATE_EXPANDED
        val resultTextView1: TextView = findViewById(R.id.disease_name)
        resultTextView1.text = "Please Wait..."


        if (bitmap != null) {
            val dimension = minOf(bitmap.width, bitmap.height)
            val thumbnail = ThumbnailUtils.extractThumbnail(bitmap, dimension, dimension)

            val resizedImage = Bitmap.createScaledBitmap(thumbnail, imageSize, imageSize, false)

            val resultIndex = ClassifyImage(resizedImage)

            val class_name = arrayOf(
                "Apple___Apple_scab",
                "Apple___Black_rot",
                "Apple___Cedar_apple_rust",
                "Apple___healthy",
                "Blueberry___healthy",
                "Cherry_(including_sour)___Powdery_mildew",
                "Cherry_(including_sour)___healthy",
                "Corn_(maize)___Cercospora_leaf_spot Gray_leaf_spot",
                "Corn_(maize)___Common_rust_",
                "Corn_(maize)___Northern_Leaf_Blight",
                "Corn_(maize)___healthy",
                "Grape___Black_rot",
                "Grape___Esca_(Black_Measles)",
                "Grape___Leaf_blight_(Isariopsis_Leaf_Spot)",
                "Grape___healthy",
                "Orange___Haunglongbing_(Citrus_greening)",
                "Peach___Bacterial_spot",
                "Peach___healthy",
                "Pepper,_bell___Bacterial_spot",
                "Pepper,_bell___healthy",
                "Potato___Early_blight",
                "Potato___Late_blight",
                "Potato___healthy",
                "Raspberry___healthy",
                "Soybean___healthy",
                "Squash___Powdery_mildew",
                "Strawberry___Leaf_scorch",
                "Strawberry___healthy",
                "Tomato___Bacterial_spot",
                "Tomato___Early_blight",
                "Tomato___Late_blight",
                "Tomato___Leaf_Mold",
                "Tomato___Septoria_leaf_spot",
                "Tomato___Spider_mites Two-spotted_spider_mite",
                "Tomato___Target_Spot",
                "Tomato___Tomato_Yellow_Leaf_Curl_Virus",
                "Tomato___Tomato_mosaic_virus",
                "Tomato___healthy"
            )
            bottomSheetBehavior.state = BottomSheetBehavior.STATE_EXPANDED

            val resultTextView: TextView = findViewById(R.id.disease_name)
            resultTextView.text = "Prediction: ${class_name[resultIndex]}"

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
}