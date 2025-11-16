package org.listenbrainz.android.ui.screens.onboarding.auth.createaccount

import org.listenbrainz.android.util.Resource

data class CreateAccountClientCallbacks(
    val onCaptchaSetupComplete: () -> Unit,
    val onLoad: (Resource<String>) -> Unit,
    val onPageLoadStateChange: (Boolean, String?) -> Unit,
)
