package org.listenbrainz.android.ui.screens.onboarding.auth

import android.webkit.WebView
import android.webkit.WebViewClient
import com.limurse.logger.Logger

class ConsentWebViewClient(
    private val onLoadData: (String) -> Unit,
): WebViewClient() {
    override fun onPageFinished(view: WebView?, url: String?) {
        super.onPageFinished(view, url)
        if (url?.contains("listenbrainz.org/login") == true) {
            view?.postDelayed(
                {
                    view.evaluateJavascript(
                        """
                    (function() {
                    var container = document.querySelector('div[role="main"].text-center');
                    var paragraphs = container.querySelectorAll('p');
                    var paragraphTexts = Array.from(paragraphs).map(p => p.innerHTML.trim()).join('\n\n');
                    return paragraphTexts;
                    })()
                    
                """.trimIndent()
                    ) { value ->
                        Logger.d("Consent screen data", "Consent Screen Data: $value")
                        if(value != null && value != "null"){
                            onLoadData(value)
                        }
                    }
                }, 1000
            )
        }
    }
}