package ru.raid.smartdiary.net

import okhttp3.MultipartBody
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Call
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.Multipart
import retrofit2.http.POST
import retrofit2.http.Part
import retrofit2.http.Query
import java.util.concurrent.TimeUnit

interface Api {
    @POST("add_user")
    fun addUser(@Query("name") name: String): Call<AddUserResponse>

    @Multipart
    @POST("record")
    fun addRecord(
            @Part("u_id") uId: Long,
            @Part data: MultipartBody.Part
    ): Call<AddRecordResponse>
}

private val retrofit = Retrofit.Builder()
        .baseUrl("http://104.43.138.252:5000/")
        .addConverterFactory(GsonConverterFactory.create())
        .client(OkHttpClient.Builder()
                .addInterceptor(
                        HttpLoggingInterceptor().apply {
                            level = HttpLoggingInterceptor.Level.BODY
                        }
                )
                .connectTimeout(3, TimeUnit.SECONDS)
                .callTimeout(0, TimeUnit.SECONDS)
                .readTimeout(24, TimeUnit.SECONDS)
                .build())
        .build()

val api = retrofit.create(Api::class.java)
