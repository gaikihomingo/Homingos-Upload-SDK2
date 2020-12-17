package com.homingos.sdk.utils

import android.content.Context
import com.homingos.sdk.utils.Constants.BASE_REDIRECTION_URL
import com.homingos.sdk.utils.Constants.BASE_REDIRECTION_URL_DEBUG
import com.homingos.sdk.utils.Constants.BUCKET_NAME
import com.homingos.sdk.utils.Constants.BUCKET_NAME_DEBUG
import com.homingos.sdk.utils.Constants.PREFIX
import com.homingos.sdk.utils.Constants.PREFIX_DEBUG
import com.homingos.sdk.utils.Constants.UPLOAD_URL
import com.homingos.sdk.utils.Constants.UPLOAD_URL_DEBUG
import java.lang.reflect.Field

fun getUploadUrl(isDebugMode: Boolean): String {
    return if (isDebugMode) {
        UPLOAD_URL_DEBUG
    } else {
        UPLOAD_URL
    }
}

fun getRedirectionUrl(isDebugMode: Boolean): String {
    return if (isDebugMode) {
        BASE_REDIRECTION_URL_DEBUG
    } else {
        BASE_REDIRECTION_URL
    }
}

fun getBucketName(isDebugMode: Boolean): String {
    return if (isDebugMode) {
        BUCKET_NAME_DEBUG
    } else {
        BUCKET_NAME
    }
}

fun getPrefix(isDebugMode: Boolean): String {
    return if (isDebugMode) {
        PREFIX_DEBUG
    } else {
        PREFIX
    }
}