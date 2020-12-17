package com.homingos.sdk.network

import java.io.Serializable

interface UrlListener: Serializable {

    fun onUrlChanged(url: String)

}