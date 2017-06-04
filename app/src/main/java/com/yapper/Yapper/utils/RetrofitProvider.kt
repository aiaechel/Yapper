package com.yapper.Yapper.utils

import com.google.gson.Gson
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object RetrofitProvider {
    val retrofit : Retrofit

    init {
        val logger = HttpLoggingInterceptor()
        logger.setLevel(HttpLoggingInterceptor.Level.BODY)
        val okClient = OkHttpClient.Builder()
                .addInterceptor(logger)
                .build()
        retrofit = Retrofit.Builder()
                .baseUrl(Constants.BACKEND_URL)
                .addConverterFactory(GsonConverterFactory.create(Gson()))
                .client(okClient)
                .build()
    }
}