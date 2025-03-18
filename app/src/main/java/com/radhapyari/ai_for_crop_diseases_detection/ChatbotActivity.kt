package com.radhapyari.ai_for_crop_diseases_detection

import android.Manifest
import android.content.pm.PackageManager
import android.media.MediaRecorder
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import java.io.File
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.*

class ChatbotActivity : ComponentActivity() {
    private var mediaRecorder: MediaRecorder? = null
    private var audioFile: File? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MaterialTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    ChatbotScreen(
                        onRecordAudio = { startRecording() },
                        onStopRecording = { onResponse ->
                            stopRecording(onResponse) // Pass the callback to stopRecording
                        }
                    )
                }
            }
        }
    }

    // Permission request launcher
    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            startRecording()
        } else {
            // Handle permission denied
        }
    }

    // Check and request permissions
    private fun checkAndRequestPermissions() {
        if (ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.RECORD_AUDIO
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            requestPermissionLauncher.launch(Manifest.permission.RECORD_AUDIO)
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(
                    this,
                    Manifest.permission.READ_MEDIA_AUDIO
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                requestPermissionLauncher.launch(Manifest.permission.READ_MEDIA_AUDIO)
            }
        }
    }

    // Start recording audio
    private fun startRecording() {
        checkAndRequestPermissions()

        if (ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.RECORD_AUDIO
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            return
        }

        mediaRecorder = MediaRecorder().apply {
            setAudioSource(MediaRecorder.AudioSource.MIC)
            setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP) // Use MP4 format
            setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB) // Use AAC encoder
            audioFile = createAudioFile()
            setOutputFile(audioFile?.absolutePath)
            try {
                prepare()
                start()
            } catch (e: IOException) {
                e.printStackTrace()
            }
        }
    }

    // Stop recording audio
    private fun stopRecording(onResponse: (String) -> Unit) {
        mediaRecorder?.apply {
            try {
                stop()
                release()
            } catch (e: IllegalStateException) {
                e.printStackTrace()
            }
        }

        // Send the recorded audio file to the server
        audioFile?.let { file ->
            sendAudioQuery(file, onResponse) // Pass the callback to sendAudioQuery
        }
        mediaRecorder = null

    }

    // Create a unique audio file in app-specific storage
    private fun createAudioFile(): File? {
        val timeStamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
        val audioName = "AUDIO_${timeStamp}_"
        val audioFileExtension = ".wav" // Change to .mp4

        val storageDir = getExternalFilesDir(null) // Use app-specific storage
        return try {
            File.createTempFile(audioName, audioFileExtension, storageDir)
        } catch (e: IOException) {
            e.printStackTrace()
            null
        }
    }

    // Function to send the audio query to the server
    private fun sendAudioQuery(file: File, onResponse: (String) -> Unit) {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val requestFile = file.asRequestBody("audio/wav".toMediaTypeOrNull())
                val body = MultipartBody.Part.createFormData("audio_file", file.name, requestFile)
                println("Sending file: ${file.name}, size: ${file.length()} bytes")
                val response = RetrofitClient.audioInstance.queryAudio(body)
                if (response.isSuccessful) {
                    val chatbotResponse = response.body()
                    val answer = chatbotResponse?.answer ?: "No response"
                    println("Server response: $answer") // Log the response
                    onResponse(answer)
                } else {
                    println("Server error: ${response.errorBody()?.string()}") // Log the error
                    onResponse("Error: ${response.errorBody()?.string()}")
                }
            } catch (e: Exception) {

                println("Network error: ${e.message}") // Log the exception
                onResponse("Network error: ${e.message}")
            }
        }
    }
    @Composable
    fun ChatbotScreen(
        onRecordAudio: () -> Unit,
        onStopRecording: (onResponse: (String) -> Unit) -> Unit
    ) {
        var question by remember { mutableStateOf("") }
        var answer by remember { mutableStateOf("") }
        var isLoading by remember { mutableStateOf(false) }
        var isRecording by remember { mutableStateOf(false) }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Input field for the text query
            OutlinedTextField(
                value = question,
                onValueChange = { question = it },
                label = { Text("Enter your question") },
                modifier = Modifier.fillMaxWidth(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Text),
                singleLine = true
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Button to send the text query
            Button(
                onClick = {
                    if (question.isNotEmpty()) {
                        isLoading = true
                        sendTextQuery(question) { response ->
                            answer = response
                            isLoading = false
                        }
                    }
                },
                enabled = !isLoading
            ) {
                Text(if (isLoading) "Loading..." else "Send Text Query")
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Button to record audio
            Button(
                onClick = {
                    if (isRecording) {
                        onStopRecording { response ->
                            answer = response // Update the answer state with the audio response
                        }
                    } else {
                        onRecordAudio()
                    }
                    isRecording = !isRecording
                },
                enabled = !isLoading
            ) {
                Text(if (isRecording) "Stop Recording" else "Record Audio")
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Display the answer
            if (answer.isNotEmpty()) {
                Text(
                    text = "Answer: $answer",
                    style = MaterialTheme.typography.bodyLarge,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }
    }
    // Function to send the text query to the server
    private fun sendTextQuery(question: String, onResponse: (String) -> Unit) {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val response = RetrofitClient.textInstance.queryText(question)
                if (response.isSuccessful) {
                    val chatbotResponse = response.body()
                    if (chatbotResponse != null) {
                        println("Full server response: $chatbotResponse") // Log the full response
                        val answer = chatbotResponse.answer
                        println("Extracted answer: $answer") // Log the extracted answer
                        onResponse(answer)
                    } else {
                        println("Server response is null") // Log if the response body is null
                        onResponse("No response")
                    }
                } else {
                    println("Server error: ${response.errorBody()?.string()}") // Log the error
                    onResponse("Error: ${response.errorBody()?.string()}")
                }
            } catch (e: Exception) {
                println("Network error: ${e.message}") // Log the exception
                onResponse("Network error: ${e.message}")
            }
        }
    }
}



