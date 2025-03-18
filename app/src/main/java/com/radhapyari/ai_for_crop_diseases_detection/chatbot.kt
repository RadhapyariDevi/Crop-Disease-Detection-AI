package com.radhapyari.ai_for_crop_diseases_detection


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