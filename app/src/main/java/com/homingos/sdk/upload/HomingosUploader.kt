package com.homingos.sdk.upload

import android.app.ActivityManager
import android.content.Context
import android.content.Intent
import android.database.Cursor
import android.media.MediaMetadataRetriever
import android.net.Uri
import android.os.Build
import android.provider.OpenableColumns
import com.homingos.sdk.browser.BrowserActivity
import com.homingos.sdk.browser.UrlCalled
import com.homingos.sdk.model.VideoPRQ
import com.homingos.sdk.model.VideoRS
import com.homingos.sdk.network.HomingosRequestBody
import com.homingos.sdk.network.RetrofitClient
import com.homingos.sdk.network.UploadProgressListener
import com.homingos.sdk.network.UrlListener
import com.homingos.sdk.utils.getBucketName
import com.homingos.sdk.utils.getPrefix
import com.homingos.sdk.utils.getRedirectionUrl
import com.homingos.sdk.utils.getUploadUrl
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.io.File
import java.util.*

class HomingosUploader private constructor(private val context: Context) : UploadProgressListener {

    private lateinit var getUrlCall: Call<VideoRS>
    private lateinit var uploadCall: Call<Void>

    private var uploadUrl: String? = null
    private var uploadDialog: UploadDialog? = null

    private val apiService by lazy { RetrofitClient.getApiService() }

    private var sdkSessionId = ""
    private var isDebugMode = false
    private val appSessionId by lazy { UUID.randomUUID().toString() }

    private var progressListener: UploadProgressListener? = null
    private var urlListener: UrlListener? = null

    companion object {
        private var uploader: HomingosUploader? = null

        @JvmStatic
        fun getInstance(context: Context): HomingosUploader {
            uploader = HomingosUploader(context)
            return uploader!!
        }
    }

    fun upload(
        uri: Uri, apiKey: String, isDebugMode: Boolean = false,
        listener: UploadProgressListener? = null, urlListener: UrlListener? = null
    ) {
        try {
            EventBus.getDefault().register(this)
        } catch (e: Exception) {
            //ignore
        }
        this.isDebugMode = isDebugMode
        this.progressListener = listener
        this.urlListener = urlListener
        RetrofitClient.setApiKey(apiKey)
        sdkSessionId = UUID.randomUUID().toString()
        ensureVideoFile(uri)
        generateSignedUrl(uri)
    }

    private fun ensureVideoFile(uri: Uri) {
        val retriever = MediaMetadataRetriever()
        retriever.setDataSource(context, uri)

        //METADATA_KEY_MIMETYPE
        val hasVideo = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_HAS_VIDEO)
        if ("yes" != hasVideo) {
            throw IllegalArgumentException("Invalid file type. Ensure file selected is a video")
        }
        if (!getFileName(uri).contains("mp4", true)
            && !getFileName(uri).contains("mov", true)
        ) {
            throw IllegalArgumentException("Unsupported video format. Video should be in mp4 or mov format")
        }
    }

    private fun generateSignedUrl(uri: Uri) {
        val path = "${context.cacheDir.absolutePath}/homingos_${getFileName(uri)}"
        val file = File(path)
        //copy file from uri to cache directory
        context.contentResolver.openInputStream(uri)?.use { inStream ->
            file.outputStream().use { inStream.copyTo(it) }
        }

        val activityManager = context.getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager
        val memoryInfo = ActivityManager.MemoryInfo()
        activityManager.getMemoryInfo(memoryInfo)
        val memory = memoryInfo.availMem.toString()

        getUrlCall = apiService.getSignedUrl(
            getUploadUrl(isDebugMode),
            VideoPRQ(
                file.name,
                getBucketName(isDebugMode),
                getPrefix(isDebugMode),
                Build.MANUFACTURER,
                Build.TYPE,
                Build.VERSION.CODENAME,
                memory,
                Runtime.getRuntime().availableProcessors().toString(),
                appSessionId,
                sdkSessionId,
            )
        )
        getUrlCall.enqueue(object : Callback<VideoRS> {
            override fun onResponse(call: Call<VideoRS>, response: Response<VideoRS>) {
                ResponseHolder.response = response.body()
                uploadUrl = ResponseHolder.response?.videoData?.uploadUrl
                uploadToAWS(uploadUrl!!, file)
            }

            override fun onFailure(call: Call<VideoRS>, t: Throwable) {
                t.printStackTrace()
            }
        })
    }

    private fun uploadToAWS(url: String, file: File) {
        uploadDialog = UploadDialog.getInstance(context) { uploadCall.cancel() }
        uploadDialog?.show()
        val fileBody = HomingosRequestBody(file, this)

        uploadCall = apiService.uploadFile(url, fileBody)
        uploadCall.enqueue(object : Callback<Void> {
            override fun onResponse(call: Call<Void>, response: Response<Void>) {
                uploadDialog?.dismiss()
                try {
                    //remove file once its uploaded
                    file.delete()
                } catch (e: Exception) {
                    e.printStackTrace()
                }
                redirectToWebPage()
            }

            override fun onFailure(call: Call<Void>, t: Throwable) {
                t.printStackTrace()
                ResponseHolder.response = null
            }
        })
    }

    private fun redirectToWebPage() {
        val intent = Intent(context, BrowserActivity::class.java)

        val url = Uri.parse(getRedirectionUrl(isDebugMode))
            .buildUpon()
            .appendQueryParameter("videoUrl", ResponseHolder.response?.videoData?.resourceUrl)
            .appendQueryParameter("source", context.packageName)
            .appendQueryParameter("appSessionId", appSessionId)
            .appendQueryParameter("sdkSessionId", sdkSessionId)
            .build()
            .toString()

        intent.putExtra(BrowserActivity.EXTRA_REDIRECTION_URL, url)
        context.startActivity(intent)
        ResponseHolder.response = null
    }

    private fun getFileName(uri: Uri): String {
        var result: String? = null
        if (uri.scheme == "content") {
            val cursor: Cursor? = context.contentResolver.query(uri, null, null, null, null)
            cursor.use { cur ->
                if (cur != null && cur.moveToFirst()) {
                    result = cur.getString(cur.getColumnIndex(OpenableColumns.DISPLAY_NAME))
                }
            }
        }
        if (result == null) {
            result = uri.path
            val index = result!!.lastIndexOf('/')
            if (index != -1) {
                result = result!!.substring(index + 1)
            }
        }
        return result.orEmpty()
    }

    override fun onProgressUpdate(percentage: Int) {
        progressListener?.onProgressUpdate(percentage)
        uploadDialog?.setProgress(percentage)
    }

    @Subscribe(sticky = true, threadMode = ThreadMode.MAIN)
    fun onUrlCalled(url: UrlCalled) {
        EventBus.getDefault().removeStickyEvent(UrlCalled::class.java)
        urlListener?.onUrlChanged(url.url)
    }

}