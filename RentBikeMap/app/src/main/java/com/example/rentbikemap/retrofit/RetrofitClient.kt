package com.example.rentbikemap.retrofit

import android.os.StrictMode
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory
import retrofit2.converter.gson.GsonConverterFactory

/* #21# Retrofit2, Rx을 사용하여 비동기 통신 */
class RetrofitClient {

    companion object {
        private const val base_url = "http://openapi.seoul.go.kr:8088/"

        fun getInstance(): RentBikeService = Retrofit.Builder()
            .baseUrl(base_url)
            .client(OkHttpClient())
            .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(RentBikeService::class.java)
    }
}