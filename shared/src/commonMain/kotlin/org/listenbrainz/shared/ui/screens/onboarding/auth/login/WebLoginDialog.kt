package org.listenbrainz.shared.ui.screens.onboarding.auth.login

import androidx.compose.runtime.Composable

/** Clears the website session, so the next [WebLoginDialog] starts logged out. */
expect fun clearWebLoginSession()

/** The website's login, full screen. Reports the user's token once they are logged in. */
@Composable
expect fun WebLoginDialog(onTokenFound: (String) -> Unit, onDismiss: () -> Unit)
