package org.listenbrainz.android.ui.screens.profile

import android.net.Uri
import android.webkit.CookieManager
import android.webkit.WebView
import android.webkit.WebViewClient
import okhttp3.Call
import okhttp3.Callback
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response
import okio.IOException
import org.jsoup.Jsoup

class ListenBrainzWebClient(private val setLBAuthToken: (String) -> Unit): WebViewClient() {

    private val client: OkHttpClient = OkHttpClient().newBuilder().build()

    override fun onPageFinished(view: WebView?, url: String?) {
        super.onPageFinished(view, url)
        val uri = Uri.parse(url)
        if (uri.host == "listenbrainz.org") {
            val cookie = CookieManager.getInstance().getCookie(url)
            if (cookie != null) {
                retrieveLBAuthToken(cookie)
            }
        }
    }

    private fun retrieveLBAuthToken(cookie: String) {
        val request = Request
            .Builder()
            .addHeader("Cookie", cookie)
            .url("https://listenbrainz.org/profile")
            .build()

        client.newCall(request).enqueue(object: Callback {
            override fun onFailure(call: Call, e: IOException) {
                e.printStackTrace()
            }

            override fun onResponse(call: Call, response: Response) {
                response.use {
                    if (response.isSuccessful) {
                        val document = Jsoup.parse(response.body.string())
                        val element = document.getElementById("auth-token")
                        val token = element?.attr("value")
                        if (!token.isNullOrEmpty()) {
                            setLBAuthToken(token)
                        }
                    }
                }
            }
        })
    }

}