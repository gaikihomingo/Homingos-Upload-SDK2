package com.homingos.sdk.network

import okhttp3.MediaType
import okhttp3.RequestBody
import okio.BufferedSink
import java.io.File
import java.io.FileInputStream
import java.io.IOException
import kotlin.jvm.Throws

internal class HomingosRequestBody(
    private val file: File,
    private val uploadEventListener: UploadProgressListener
) : RequestBody() {

    override fun contentType() = MediaType.parse("video/*")

    @Throws(IOException::class)
    override fun contentLength() = file.length()

    override fun writeTo(sink: BufferedSink) {
        val fileLength: Long = file.length()
        val buffer = ByteArray(DEFAULT_BUFFER_SIZE)
        val inputStream = FileInputStream(file)
        var uploaded: Long = 0

        inputStream.use { inStream ->
            var read: Int
            while (inStream.read(buffer).also { read = it } != -1) {
                uploaded += read.toLong()
                sink.write(buffer, 0, read)
                val progress = (100 * uploaded / fileLength).toInt()
                uploadEventListener.onProgressUpdate(percentage = progress)
            }
        }
    }

}