package org.listenbrainz.android.ui.screens.onboarding.auth.createaccount

import android.webkit.JavascriptInterface
import org.listenbrainz.shared.util.Log

class CreateAccountWebAppInterface(
    private val onCaptchaVerificationCompleted: () -> Unit,
    private val logger:Log = Log
) {
    @JavascriptInterface
    fun onCaptchaVerified() {
        onCaptchaVerificationCompleted()
        logger.d("Captcha verified callback received from WebView", tag = "CreateAccountWebAppInterface")
    }
}