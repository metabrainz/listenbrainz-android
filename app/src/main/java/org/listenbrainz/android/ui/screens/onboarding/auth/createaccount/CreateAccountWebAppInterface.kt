package org.listenbrainz.android.ui.screens.onboarding.auth.createaccount

import android.webkit.JavascriptInterface
import org.listenbrainz.android.util.Log

class CreateAccountWebAppInterface(
    private val onCaptchaVerificationCompleted: () -> Unit
) {
    @JavascriptInterface
    fun onCaptchaVerified() {
        onCaptchaVerificationCompleted()
        Log.d("Captcha verified callback received from WebView", tag = "CreateAccountWebAppInterface")
    }
}