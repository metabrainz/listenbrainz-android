package org.listenbrainz.android.ui.screens.onboarding.auth

import android.graphics.Bitmap
import android.webkit.WebView
import android.webkit.WebViewClient
import com.limurse.logger.Logger

class ConsentWebViewClient(
    private val onLoadData: (String) -> Unit,
    private val onError: (String) -> Unit = {}
) : WebViewClient() {
    val TAG = "ConsentWebViewClient"
    private var hadError = false

    override fun onPageStarted(view: WebView?, url: String?, favicon: Bitmap?) {
        super.onPageStarted(view, url, favicon)
        hadError = false
        Logger.d(TAG, "Page started loading: $url")
    }


    override fun onPageFinished(view: WebView?, url: String?) {
        super.onPageFinished(view, url)
        if (hadError) return
        Logger.d(TAG, "Page finished loading: $url")

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
                        return "Error: " + e.message;
                      }
                    })()
                    
                """.trimIndent()
                    ) { value ->
                        Logger.d(TAG, "Consent Screen Data: $value")
                        if (value != null && value != "null" && !value.startsWith("Error:")) {
                            onLoadData(value)
                        } else {
                            Logger.d(
                                TAG,
                                "Failed to retrieve consent screen data or no consent text found."
                            )
                            onError("Could not retrieve consent information. Please try again.")
                        }
                    }
                }, 2000
            )
        }
    }
}