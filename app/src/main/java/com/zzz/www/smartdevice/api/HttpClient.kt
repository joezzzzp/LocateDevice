package com.zzz.www.smartdevice.api

import com.google.gson.Gson
import com.google.gson.GsonBuilder
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.adapter.rxjava.RxJavaCallAdapterFactory
import retrofit2.converter.gson.GsonConverterFactory

/**
 * @author zzz
 * @date create at 2018/1/3.
 */
class HttpClient private constructor() {

    companion object {
        @Volatile private var instance : Retrofit? = null

        fun getClient() : Retrofit =
            instance ?: synchronized(this) {
                instance ?: createRetrofitInstance().also { instance = it }
            }

        private fun createRetrofitInstance() : Retrofit {
            val gson: Gson = GsonBuilder().setDateFormat("yyyy-MM-dd HH:mm:ss").create()
            val okHttpClient: OkHttpClient = OkHttpClient.Builder()
                    .addInterceptor(HttpLoggingInterceptor()
                            .setLevel(HttpLoggingInterceptor.Level.BODY)).build()
            return Retrofit.Builder()
                    .baseUrl(HttpConfig.baseUrl)
                    .client(okHttpClient)
                    .addCallAdapterFactory(RxJavaCallAdapterFactory.create())
                    .addConverterFactory(GsonConverterFactory.create(gson))
                    .build()
        }
    }
}