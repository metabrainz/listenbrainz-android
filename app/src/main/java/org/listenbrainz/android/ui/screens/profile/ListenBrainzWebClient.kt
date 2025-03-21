package org.listenbrainz.android.ui.screens.profile

import android.net.Uri
import android.webkit.WebView
import android.webkit.WebViewClient
import com.limurse.logger.Logger
import org.listenbrainz.android.util.Resource

class ListenBrainzWebClient(
    private val onLoad: (Resource<String>) -> Unit,
) : WebViewClient() {

    private var attemptedSettingsNavigation = false

    override fun onPageFinished(view: WebView?, url: String?) {
        super.onPageFinished(view, url)
        Logger.d("ListenBrainzWebClient", "onPageFinished URL: $url")

        if (url == null) {
            Logger.d("ListenBrainzWebClient", "URL is null")
            return
        }

        val uri = Uri.parse(url)

        Logger.d("ListenBrainzWebClient", "Host: ${uri.host}, Path: ${uri.path}")

        if (uri.host == "listenbrainz.org") {
            when {
                !attemptedSettingsNavigation -> {
                    Logger.d("ListenBrainzWebClient", "Navigating to settings page")
                    attemptedSettingsNavigation = true
                    view?.loadUrl("https://listenbrainz.org/settings")
                }
                uri.path?.contains("/settings") == true -> {
                    onLoad(Resource.loading())
                    Logger.d("ListenBrainzWebClient", "On settings page, waiting to extract token...")

                    view?.postDelayed(
                        /*action = */{
                            view.evaluateJavascript(
                                "(function() { return document.getElementById('auth-token') ? document.getElementById('auth-token').value : 'not found'; })();"
                            ) { value ->
                                val token = value.removePrefix("\"").removeSuffix("\"")
                                when {
                                    token.isNotEmpty() && token != "not found" -> {
                                        onLoad(Resource.success(token))
                                    }
                                    else -> {
                                        Logger.d("ListenBrainzWebClient", "Token not found or empty")
                                        onLoad(Resource.failure())
                                    }
                                }
                            }
                        },
                        /*delayMillis =*/2000
                    )
                }
            }
        }
    }
}