package org.listenbrainz.shared.ui.screens.onboarding.auth.login

import io.ktor.http.Url

/**
 * Logs in on the ListenBrainz website and reads the user token from the first page that has it.
 *
 * Every server rendered ListenBrainz page embeds `current_user.auth_token` in its
 * `global-react-props` JSON once the user is logged in, so nothing on the login pages is touched.
 */
object WebLogin {
    const val LOGIN_URL = "https://listenbrainz.org/login/musicbrainz/?next=/settings/"

    /** Evaluates to the user token, or null while logged out. */
    const val TOKEN_SCRIPT = """
        (function () {
          try {
            var props = document.getElementById('global-react-props');
            if (!props) return null;
            var user = JSON.parse(props.textContent).current_user;
            return (user && user.auth_token) || null;
          } catch (e) {
            return null;
          }
        })();
    """

    fun isListenBrainzPage(url: String?): Boolean =
        url != null && runCatching { Url(url).host == "listenbrainz.org" }.getOrDefault(false)
}
