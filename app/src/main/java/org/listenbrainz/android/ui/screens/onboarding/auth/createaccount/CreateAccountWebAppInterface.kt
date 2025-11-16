package org.listenbrainz.android.ui.screens.onboarding.auth.createaccount

import android.webkit.JavascriptInterface
import com.limurse.logger.Logger

class CreateAccountWebAppInterface(
    private val onCaptchaVerificationCompleted: () -> Unit
) {
    @JavascriptInterface
    fun onCaptchaVerified() {
        onCaptchaVerificationCompleted()
        Logger.d("CreateAccountWebAppInterface", "Captcha verified callback received from WebView")
    }
}