package com.radhapyari.ai_for_crop_diseases_detection

import okhttp3.MultipartBody
import retrofit2.Response
import retrofit2.http.*

interface ApiService {
    // Endpoint for text queries (connect.py)
    @GET("query/")
    suspend fun queryText(@Query("question") question: String): Response<ChatbotResponse>

    // Endpoint for audio queries (user_voice.py)
    @Multipart
    @POST("transcribe-and-query/")
    suspend fun queryAudio(@Part file: MultipartBody.Part): Response<ChatbotResponse>
}