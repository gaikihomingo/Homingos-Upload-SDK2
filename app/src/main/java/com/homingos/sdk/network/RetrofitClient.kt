package com.homingos.sdk.network

import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

internal class RetrofitClient private constructor() {

    companion object {

        private var client: Retrofit? = null

        private var apiService: HomingosApiService? = null

        private var apiKey: String = ""

        /**
         * @return - Instance of [HomingosApiService]
         */
        fun getApiService(): HomingosApiService {
            if (apiService == null) {
                apiService = getInstance().create(HomingosApiService::class.java)
            }
            return apiService!!
        }

        fun setApiKey(apiKey: String) {
            this.apiKey = apiKey
        }

        /**
         * @return Singleton instance of [Retrofit]
         */
        private fun getInstance(): Retrofit {
            if (client == null) {
                client = Retrofit.Builder()
                    .baseUrl("https://www.homingos.com/")
                    .client(getClient())
                    .addConverterFactory(GsonConverterFactory.create())
                    .build()
            }
            return client!!
        }

        /**
         * @return [OkHttpClient] with [HeaderInterceptor]
         */
        private fun getClient(): OkHttpClient {
            return OkHttpClient.Builder()
                .addInterceptor(HeaderInterceptor(apiKey))
                .build()
        }
    }

}