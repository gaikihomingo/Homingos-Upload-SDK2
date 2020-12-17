package com.homingos.sdk.model

import androidx.annotation.Keep
import com.google.gson.annotations.SerializedName

@Keep
internal data class VideoRS(
    @SerializedName("data")
    val videoData: VideoRSData,
    @SerializedName("message")
    val message: String,
    @SerializedName("status")
    val status: Int,
    @SerializedName("error")
    val error: Boolean
)

@Keep
internal data class VideoRSData(
    @SerializedName("uploadUrl")
    val uploadUrl: String,
    @SerializedName("filename")
    val filename: String,
    @SerializedName("resourceUrl")
    val resourceUrl: String
)

@Keep
internal data class VideoPRQ(
    @SerializedName("filename")
    val filename: String,
    @SerializedName("bucketName")
    val bucketName: String,
    @SerializedName("prefix")
    val prefix: String,
    @SerializedName("deviceName")
    val deviceName: String,
    @SerializedName("deviceType")
    val deviceType: String,
    @SerializedName("operatingSystem")
    val os: String,
    @SerializedName("systemMemorySize")
    val systemMemorySize: String,
    @SerializedName("processorCount")
    val processorCount: String,
    @SerializedName("appSession")
    val appSession: String,
    @SerializedName("sdkSession")
    val sdkSession: String,
    @SerializedName("contentType")
    val contentType: String = "video/*"
)