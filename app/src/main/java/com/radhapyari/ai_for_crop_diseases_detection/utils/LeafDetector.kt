package com.radhapyari.ai_for_crop_diseases_detection.utils

import android.content.Context
import android.graphics.Bitmap
import org.tensorflow.lite.DataType
import org.tensorflow.lite.Interpreter
import org.tensorflow.lite.support.common.FileUtil
import org.tensorflow.lite.support.common.ops.CastOp
import org.tensorflow.lite.support.common.ops.NormalizeOp
import org.tensorflow.lite.support.image.ImageProcessor
import org.tensorflow.lite.support.image.TensorImage
import org.tensorflow.lite.support.tensorbuffer.TensorBuffer


class LeafDetector(
    private val context: Context,
    private val modelPath: String
) {

    private var interpreter: Interpreter? = null

    private var tensorWidth = 0
    private var tensorHeight = 0
    private var numChannel = 0
    private var numElements = 0


    private val imageProcessor = ImageProcessor.Builder()
        .add(NormalizeOp(INPUT_MEAN, INPUT_STANDARD_DEVIATION))
        .add(CastOp(INPUT_IMAGE_TYPE))
        .build()



    fun setUp() {
        val model = FileUtil.loadMappedFile(context, modelPath)

        val options = Interpreter.Options()
        interpreter = Interpreter(model,options)

        val inputShape = interpreter?.getInputTensor(0)?.shape() ?: return
        val outputShape = interpreter?.getOutputTensor(0)?.shape() ?: return


        tensorWidth = inputShape[1] //640
        tensorHeight = inputShape[2] //640
        numChannel = outputShape[1] //5
        numElements = outputShape[2] //8400

    }

    fun clear(){
        interpreter?.close()
        interpreter = null
    }

    fun detectLeaf(frame: Bitmap): Boolean {
        interpreter ?: return false
        if (tensorWidth == 0) return false
        if (tensorHeight == 0) return false
        if (numChannel == 0) return false
        if (numElements == 0) return false

        val resized = Bitmap.createScaledBitmap(frame,tensorWidth,tensorHeight, false)
        val tensorImage = TensorImage(DataType.FLOAT32)
        tensorImage.load(resized)
        val processed   = imageProcessor.process(tensorImage)
        val inputBuffer = processed.buffer

        val output = TensorBuffer.createFixedSize(intArrayOf(1 , numChannel, numElements), OUTPUT_IMAGE_TYPE)
        interpreter?.run(inputBuffer, output.buffer)

        return hasLeaf(output.floatArray)

    }


    private fun hasLeaf(array: FloatArray) : Boolean {

        val confStartIndex = 4 * numElements

        for(i in 0 until numElements){
            if(array[confStartIndex + i] > CONFIDENCE_THRESHOLD){
                return true
            }
        }

        return false
    }


    companion object {
        private const val INPUT_MEAN = 0f
        private const val INPUT_STANDARD_DEVIATION = 255f
        private val INPUT_IMAGE_TYPE = DataType.FLOAT32
        private val OUTPUT_IMAGE_TYPE = DataType.FLOAT32
        private const val CONFIDENCE_THRESHOLD = 0.6F
        private const val IOU_THRESHOLD = 0.5F
    }


}







