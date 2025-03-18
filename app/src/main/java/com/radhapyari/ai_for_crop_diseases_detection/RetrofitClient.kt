import com.radhapyari.ai_for_crop_diseases_detection.ApiService

import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

object RetrofitClient {
    // Base URLs for the two FastAPI services
    private const val BASE_URL_TEXT = "http://192.168.156.253:8000/" // Text query server (connect.py)
    private const val BASE_URL_AUDIO = "http://192.168.156.253:8001/" // Audio query server (user_voice.py)

    //Retrofit instance for text queries
    private val okHttpClient = OkHttpClient.Builder()
        .connectTimeout(60, TimeUnit.SECONDS) // Increase connection timeout
        .readTimeout(60, TimeUnit.SECONDS)    // Increase read timeout
        .writeTimeout(60, TimeUnit.SECONDS)   // Increase write timeout
        .addInterceptor { chain ->
            val request = chain.request()
            println("Request URL: ${request.url}")
            println("Request Headers: ${request.headers}")
            chain.proceed(request)
        }
        .build()

    val textInstance: ApiService by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL_TEXT)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(ApiService::class.java)
    }

    // Retrofit instance for audio queries
    val audioInstance: ApiService by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL_AUDIO)
            .client(okHttpClient) // Pass OkHttpClient here ✅
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(ApiService::class.java)
    }
}