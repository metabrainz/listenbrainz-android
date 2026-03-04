package org.listenbrainz.android.ui.screens.onboarding.auth

import android.graphics.Bitmap
import android.webkit.WebView
import android.webkit.WebViewClient
import org.listenbrainz.android.util.Log

class ConsentWebViewClient(
    private val onLoadData: (String) -> Unit,
    private val onError: (String) -> Unit = {}
) : WebViewClient() {
    val TAG = "ConsentWebViewClient"
    private var hadError = false

    override fun onPageStarted(view: WebView?, url: String?, favicon: Bitmap?) {
        super.onPageStarted(view, url, favicon)
        hadError = false
        Log.d("Page started loading: $url", tag = TAG)
    }


    override fun onPageFinished(view: WebView?, url: String?) {
        super.onPageFinished(view, url)
        if (hadError) return
        Log.d("Page finished loading: $url", tag = TAG)

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
                        Log.d("Consent Screen Data: $value", tag = TAG)
                        if (value != null && value != "null" && !value.startsWith("Error:")) {
                            onLoadData(value)
                        } else {
                            Log.d(
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