package org.listenbrainz.android.ui.screens.onboarding.auth

import android.graphics.Bitmap
import android.webkit.WebView
import android.webkit.WebViewClient
import org.listenbrainz.android.util.Resource

class CreateAccountWebViewClient(
    private val onPageLoadStateChange: (Boolean)->Unit,
    private val onAccountCreated: ()->Unit
): WebViewClient() {
    override fun onPageStarted(
        view: WebView?,
        url: String?,
        favicon: Bitmap?
    ) {
        super.onPageStarted(view, url, favicon)
        onPageLoadStateChange(true)
        if(url?.contains("musicbrainz.org/user") == true){
            onAccountCreated()
        }
    }

    override fun onPageFinished(view: WebView?, url: String?) {
        super.onPageFinished(view, url)
        onPageLoadStateChange(false)
    }
}