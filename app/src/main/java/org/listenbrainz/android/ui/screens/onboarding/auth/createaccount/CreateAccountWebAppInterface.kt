package org.listenbrainz.android.ui.screens.onboarding.auth.createaccount

import android.webkit.JavascriptInterface

class CreateAccountWebAppInterface(
    private val onCaptchaVerified: () -> Unit
) {
    @JavascriptInterface
    fun onCaptchaVerified() {
        onCaptchaVerified()
    }
}