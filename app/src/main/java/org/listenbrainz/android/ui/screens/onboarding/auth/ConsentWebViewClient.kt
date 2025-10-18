package org.listenbrainz.android.ui.screens.onboarding.auth

import android.graphics.Bitmap
import android.net.http.SslError
import android.webkit.SslErrorHandler
import android.webkit.WebResourceError
import android.webkit.WebResourceRequest
import android.webkit.WebView
import android.webkit.WebViewClient
import com.limurse.logger.Logger

class ConsentWebViewClient(
    private val onLoadData: (String) -> Unit,
    private val onError: (String) -> Unit = {}
): WebViewClient() {

    private var hadError = false

    override fun onPageStarted(view: WebView?, url: String?, favicon: Bitmap?) {
        super.onPageStarted(view, url, favicon)
        hadError = false
    }


    override fun onPageFinished(view: WebView?, url: String?) {
        super.onPageFinished(view, url)
        if (hadError) return

        if (url?.contains("listenbrainz.org/login") == true) {
            view?.postDelayed(
                {
                    view.evaluateJavascript(
                        """
                    (function() {
                      try {
                        var container = document.querySelector('div[role="main"].text-center');
                        if (!container) {
                          return null;
                        }
                        var paragraphs = container.querySelectorAll('p');
                        var paragraphTexts = Array.from(paragraphs).map(p => p.innerHTML.trim()).join('\n\n');
                        return paragraphTexts;
                      } catch (e) {
                        return null;
                      }
                    })()
                    
                """.trimIndent()
                    ) { value ->
                        if(value != null && value != "null"){
                            onLoadData(value)
                        } else {
                            onError("Could not retrieve consent information. Please try again.")
                        }
                    }
                }, 2000
            )
        }
    }
}