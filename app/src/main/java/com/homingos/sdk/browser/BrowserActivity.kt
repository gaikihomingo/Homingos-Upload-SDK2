package com.homingos.sdk.browser

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.MenuItem
import android.view.ViewGroup
import android.webkit.WebResourceResponse
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.FrameLayout
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import com.homingos.sdk.R
import org.greenrobot.eventbus.EventBus

internal class BrowserActivity : AppCompatActivity() {

    companion object {
        const val EXTRA_REDIRECTION_URL = "redirection_url"
    }

    private var url: String? = null
    private lateinit var toolbar: Toolbar
    private lateinit var webView: WebView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_browser)

        init()
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == android.R.id.home) {
            finish()
            return true
        }
        return super.onOptionsItemSelected(item)
    }

    private fun init() {
        extractDataFromIntent()
        setupToolbar()
        addWebView()
        loadWebPage()
    }

    private fun extractDataFromIntent() {
        val extras = intent.extras
        url = extras?.getString(EXTRA_REDIRECTION_URL)
        if (url == null) {
            throw IllegalArgumentException("Missing redirection url in intent extras")
        }
    }

    private fun setupToolbar() {
        toolbar = findViewById(R.id.toolbar)
        setSupportActionBar(toolbar)
        supportActionBar?.apply {
            title = getString(R.string.homingos)
            setDisplayHomeAsUpEnabled(true)
            setHomeAsUpIndicator(R.drawable.ic_close)
        }
    }

    private fun addWebView() {
        val webView = WebView(this.applicationContext)
        webView.layoutParams = ViewGroup.LayoutParams(
            ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT
        )
        this.webView = webView
        val container = findViewById<FrameLayout>(R.id.webViewContainer)
        container.addView(webView)

        webView.webViewClient = object : WebViewClient() {
            override fun shouldInterceptRequest(
                view: WebView,
                url: String
            ): WebResourceResponse? {
                EventBus.getDefault().postSticky(UrlCalled(url))
                return super.shouldInterceptRequest(view, url)
            }
        }
    }

    @SuppressLint("SetJavaScriptEnabled")
    private fun loadWebPage() {
        webView.settings.apply {
            javaScriptEnabled = true
            displayZoomControls = false
            setSupportZoom(false)
            allowContentAccess = true
            domStorageEnabled = true
        }
        webView.loadUrl(url!!)
    }

}

data class UrlCalled(val url: String)