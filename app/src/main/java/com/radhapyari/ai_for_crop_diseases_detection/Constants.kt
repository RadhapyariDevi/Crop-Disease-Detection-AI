package com.radhapyari.ai_for_crop_diseases_detection
import android.Manifest
object Constants {
    const val TAG =  "cameraX"
    const val FILE_NAME_FORMAT = "yy-MM-dd-HH-mm-ss-SSS"
    const val REQUEST_CODE_PERMISSIONS = 123
    val REQUIRED_PERMISSIONS = arrayOf(Manifest.permission.CAMERA)
    const val MODEL_PATH = "yolo.tflite"

}