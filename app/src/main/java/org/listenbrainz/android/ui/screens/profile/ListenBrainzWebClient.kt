package org.listenbrainz.android.ui.screens.profile

import android.graphics.Bitmap
import android.net.Uri
import android.webkit.WebView
import android.webkit.WebViewClient
import com.limurse.logger.Logger
import org.listenbrainz.android.model.ResponseError
import org.listenbrainz.android.util.Resource

const val TAG = "ListenBrainzWebClient"

class ListenBrainzWebClient(
    private val username: String,
    private val password: String,
    private val onLoad: (Resource<String>) -> Unit,
    private val onPageLoadStateChange: (Boolean) -> Unit,
) : WebViewClient() {

    private var attemptedSettingsNavigation = false
    private var attemptRedirectToLoginEndpoint = false

    override fun onPageStarted(
        view: WebView?,
        url: String?,
        favicon: Bitmap?
    ) {
        super.onPageStarted(view, url, favicon)
        onPageLoadStateChange(true)
    }


    override fun onPageFinished(view: WebView?, url: String?) {
        super.onPageFinished(view, url)
        onPageLoadStateChange(false)
        Logger.d(TAG, "onPageFinished URL: $url")

        if (url == null) {
            Logger.d(TAG, "URL is null")
            return
        }

        val uri = Uri.parse(url)

        Logger.d(TAG, "Host: ${uri.host}, Path: ${uri.path}")

        attemptLogin(view, url, username, password) { error ->
            onLoad(Resource.failure(error = ResponseError.BAD_REQUEST.apply {
                actualResponse = error
            }))
        }

        if (uri.host == "listenbrainz.org") {
            when {
                !attemptRedirectToLoginEndpoint ->{
                    Logger.d(TAG, "Attempting to redirect to login endpoint")
                    attemptRedirectToLoginEndpoint = true
                    view?.loadUrl("https://listenbrainz.org/login/musicbrainz")
                }
                !attemptedSettingsNavigation -> {
                    Logger.d(TAG, "Navigating to settings page")
                    attemptedSettingsNavigation = true
                    view?.postDelayed(
                        {
                            view.loadUrl("https://listenbrainz.org/settings")
                        },
                        /*delayMillis =*/ 1000
                    )
                }

                uri.path?.contains("/settings") == true -> {
                    onLoad(Resource.loading())
                    Logger.d(TAG, "On settings page, waiting to extract token...")

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
                                        Logger.d(TAG, "Token not found or empty")
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

    fun attemptLogin(
        view: WebView?,
        url: String?,
        username: String,
        password: String,
        onError: (String) -> Unit
    ) {
        if (url == null || !url.contains("musicbrainz.org//login")) {
            Logger.d(TAG, "Not on login page, cannot attempt login")
            onError("Not on login page")
            return
        }
        if (view == null) {
            Logger.d(TAG, "WebView is null, cannot attempt login")
            onError("WebView is null")
            return
        }
        val loginScript = """
            (function(){
            try{
            var formContainer = document.getElementById('page');
            if(!formContainer) return "Error: Form container not found";
            document.getElementById('id-username').value = '$username';
            document.getElementById('id-password').value = '$password';
            document.querySelector('form').submit();
            return "Login submitted";
            } catch (e){
            return "Error: " + e.message;
            }
            })();
        """.trimIndent()
        view.postDelayed({
            view.evaluateJavascript(loginScript,) { result ->
                Logger.d(TAG, "Login attempt result: $result")
                if (result == "\"Login submitted\"") {
                    Logger.d(TAG, "Login script executed successfully")
                } else {
                    Logger.e(TAG, "Error executing login script: $result")
                    onError("Error executing login script: $result")
                }

            }
        },
            1000)
    }
}