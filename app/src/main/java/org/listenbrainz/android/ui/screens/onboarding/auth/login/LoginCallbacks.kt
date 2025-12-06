package org.listenbrainz.android.ui.screens.onboarding.auth.login

import org.listenbrainz.android.util.Resource

data class LoginCallbacks(
    val onMusicBrainzLoginFormLoaded: ()-> Unit,
    val onLoad: (Resource<String>) -> Unit,
    val showGDPRConsentPrompt: () -> Unit,
    val showOAuthAuthorizationPrompt: () -> Unit,
    val onOAuthAllowClick: () -> Unit,
    val onOAuthCancelClick: () -> Unit,
    val onGDPRAgreeClick: () -> Unit,
    val onGDPRDisagreeClick: () -> Unit
)
