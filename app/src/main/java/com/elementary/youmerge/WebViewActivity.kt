package com.elementary.youmerge

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.View
import android.webkit.WebChromeClient
import android.webkit.WebSettings
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.ImageView
import androidx.appcompat.app.AppCompatActivity

class WebViewActivity : AppCompatActivity() {


    private lateinit var webView: WebView
    private lateinit var closeWebView: ImageView

    @SuppressLint("SetJavaScriptEnabled")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_webview)

        val content: String = intent.getStringExtra("content")!!

        closeWebView = findViewById(R.id.closeWebView)
        closeWebView.setOnClickListener { finish() }

        webView = findViewById(R.id.webView)

        webView.webViewClient = WebViewClient()
        webView.settings.javaScriptEnabled = true
        webView.settings.javaScriptCanOpenWindowsAutomatically = true
        webView.settings.pluginState = WebSettings.PluginState.ON
        webView.settings.mediaPlaybackRequiresUserGesture = false
        webView.webChromeClient = WebChromeClient()
//        webView.loadDataWithBaseURL(null, content, "text/html", "UTF-8", null)
        webView.loadUrl(content)


    }

}