package org.listenbrainz.android.ui.screens.onboarding.auth.login

import android.util.Log
import android.webkit.JavascriptInterface

class LoginWebViewInterface(
    private val callbacks: LoginCallbacks
) {
    private val TAG = "LoginWebViewInterface"
    @JavascriptInterface
    fun onAllowClicked(){
        callbacks.onOAuthAllowClick()
    }

    @JavascriptInterface
    fun onCancelClicked(){
        callbacks.onOAuthCancelClick()
    }


    @JavascriptInterface
    fun onGDPRSuccess() {
        Log.d(TAG, "User selected: AGREE")
    }

    @JavascriptInterface
    fun onGDPRDelete() {
        Log.d(TAG, "User selected: DISAGREE")
    }

    @JavascriptInterface
    fun onGDPRSubmitSuccess() {
        Log.d(TAG, "Submit clicked → AGREE")
        callbacks.onGDPRAgreeClick()
    }

    @JavascriptInterface
    fun onGDPRSubmitDelete() {
        Log.d(TAG, "Submit clicked → DISAGREE")
        callbacks.onGDPRDisagreeClick()
    }
}