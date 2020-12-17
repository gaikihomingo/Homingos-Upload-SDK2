package com.homingos.sdk.network

import com.homingos.sdk.model.VideoPRQ
import com.homingos.sdk.model.VideoRS
import okhttp3.MultipartBody
import okhttp3.RequestBody
import org.json.JSONObject
import retrofit2.Call
import retrofit2.http.*

internal interface HomingosApiService {

    @POST
    fun getSignedUrl(@Url url: String, @Body body: VideoPRQ): Call<VideoRS>

    @PUT
    fun uploadFile(@Url url: String, @Body file: RequestBody): Call<Void>

}