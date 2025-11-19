package org.listenbrainz.android.ui.screens.profile

import android.graphics.Bitmap
import android.net.Uri
import android.os.Build
import android.webkit.WebResourceError
import android.webkit.WebResourceRequest
import android.webkit.WebView
import android.webkit.WebViewClient
import com.limurse.logger.Logger
import org.listenbrainz.android.model.ResponseError
import org.listenbrainz.android.util.Resource
import androidx.core.net.toUri
import androidx.core.view.postDelayed
import org.listenbrainz.android.ui.screens.onboarding.auth.login.LoginCallbacks

private const val TAG = "ListenBrainzWebClient"

class ListenBrainzWebClient(
    private val callbacks: LoginCallbacks
) : WebViewClient() {

    // Track auth flow state
    private var hasLoginFormReadyCallbackBeenMade = false
    private var hasTriedFormSubmission = false
    private var hasTriedRedirectToLoginEndpoint = false
    private var hasTriedSettingsNavigation = false
    private var isLoginFailed = false
    private var currentPage: String? = null
    private var hasTriedOAuthAuthentication = false
    private var webView: WebView? = null

    override fun onPageStarted(view: WebView?, url: String?, favicon: Bitmap?) {
        super.onPageStarted(view, url, favicon)

        // Update UI with current page info
        url?.let {
            val uri = it.toUri()
//            currentPage = when {
//                uri.host == "musicbrainz.org" && uri.path == "/login" ->
//                    "Connecting to MusicBrainz..."
//
//                uri.host == "listenbrainz.org" && uri.path == "/login" ->
//                    "Connecting to ListenBrainz..."
//
//                uri.host == "listenbrainz.org" && uri.path == "/login/musicbrainz" ->
//                    "Authenticating with ListenBrainz..."
//
//                uri.host == "listenbrainz.org" && uri.path?.contains("/settings") == true ->
//                    "Retrieving authentication token..."
//
//                else -> "Loading page..."
//            }
//            callbacks.onPageLoadStateChange(true, currentPage)
            Logger.d(TAG, "Loading: $url")
        }
    }

    override fun onReceivedError(
        view: WebView?,
        request: WebResourceRequest?,
        error: WebResourceError?
    ) {
        super.onReceivedError(view, request, error)

        val errorMsg = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M)
            "Error loading page: ${error?.description}"
        else "Error loading page"
        Logger.e(TAG, errorMsg)

        //Not responding to these errors as they are not critical to the auth flow just logging them
//        onLoad(Resource.failure(error = ResponseError.BAD_REQUEST.apply {
//            actualResponse = errorMsg
//        }))
    }

    override fun onPageFinished(view: WebView?, url: String?) {
        super.onPageFinished(view, url)

        if (url == null) {
            callbacks.onLoad(Resource.failure(error = ResponseError.BAD_REQUEST.apply {
                actualResponse = "URL is null, cannot proceed with login"
            }))
            return
        }
        webView = view

        val uri = url.toUri()
        Logger.d(TAG, "Page loaded: ${uri.host}${uri.path}")

        when {
            // Check for login errors on MusicBrainz login page
            uri.host == "musicbrainz.org" && uri.path == "/login" && hasTriedFormSubmission -> {
                checkForLoginErrors(view)
            }

            // Submit login form on MusicBrainz login page
            !hasTriedFormSubmission && uri.host == "musicbrainz.org" && uri.path == "/login" && !hasLoginFormReadyCallbackBeenMade -> {
                hasLoginFormReadyCallbackBeenMade = true
                callbacks.onMusicBrainzLoginFormLoaded()
                Logger.d(TAG, "Login form is ready")
            }

            //Edge case 1: User's account is not linked with ListenBrainz
            uri.path?.contains("new-oauth2/authorize") == true && !hasTriedOAuthAuthentication && !hasTriedSettingsNavigation -> {
                Logger.e(TAG, "Account not linked with ListenBrainz, running script to accept")
                hasTriedOAuthAuthentication = true
                view?.postDelayed({
                    showOAuthPrompt(view)
                }, 2000)
            }


            // Handle ListenBrainz navigation flow
            uri.host == "listenbrainz.org" -> {
                handleListenBrainzNavigation(view, uri)
            }
        }
    }

    private fun checkForLoginErrors(view: WebView?) {
        view?.evaluateJavascript(
            "(function() { return document.querySelector('.error') ? document.querySelector('.error').textContent : null; })()"
        ) { result ->
            if (result != "null") {
                val errorMsg = result.replace("\"", "").trim()
                Logger.e(TAG, "Login failed: $errorMsg")
                isLoginFailed = true

                callbacks.onLoad(Resource.failure(error = ResponseError.BAD_REQUEST.apply {
                    actualResponse = errorMsg
                }))
            }
        }
    }

    private fun handleListenBrainzNavigation(view: WebView?, uri: Uri) {
        when {
            // Step 1: Redirect to login endpoint
            !hasTriedRedirectToLoginEndpoint -> {
                Logger.d(TAG, "Redirecting to login endpoint")
                hasTriedRedirectToLoginEndpoint = true
                view?.loadUrl("https://listenbrainz.org/login/musicbrainz")
            }

            // Edge case 2: Data protection terms not accepted. This is not a blocker for login, so we
            // can skip this step.
            uri.path?.contains("agree-to-terms") == true -> {
                showGDPRPrompt(view)
//                view?.postDelayed(2000) {
//                    checkForEmailVerificationError(view) {
//                        view.loadUrl("https://listenbrainz.org/settings")
//                    }
//                }
            }

            // Step 2: Navigate to settings to get token with edge case 2
            !hasTriedSettingsNavigation -> {
                view?.postDelayed({
                    checkForEmailVerificationError(view) {
                        if(!hasTriedSettingsNavigation) {
                            Logger.d(TAG, "Navigating to settings page")
                            hasTriedSettingsNavigation = true
                            view.loadUrl("https://listenbrainz.org/settings")
                        }
                    }
                }, 2000)
            }

            // Step 3: Extract token from settings page
            uri.path?.contains("/settings") == true -> {
                callbacks.onLoad(Resource.loading())
                Logger.d(TAG, "Extracting token from settings page")

                view?.postDelayed({
                    extractToken(view)
                }, 2000)
            }
        }
    }

    private fun extractToken(view: WebView?) {
        view?.evaluateJavascript(
            "(function() { return document.getElementById('auth-token') ? document.getElementById('auth-token').value : 'not found'; })();"
        ) { value ->
            val token = value.removePrefix("\"").removeSuffix("\"")
            if (token.isNotEmpty() && token != "not found") {
                Logger.d(TAG, "Auth token found")
                callbacks.onLoad(Resource.success(token))
            } else {
                Logger.e(TAG, "Auth token not found")
                callbacks.onLoad(Resource.failure(error = ResponseError.BAD_REQUEST.apply {
                    actualResponse = "Could not retrieve authentication token"
                }))
            }
        }
    }

    fun submitLoginForm(username: String, password: String) {
        // Validate inputs
        if (username.isEmpty() || password.isEmpty()) {
            callbacks.onLoad(Resource.failure(error = ResponseError.BAD_REQUEST.apply {
                actualResponse = "Username and password cannot be empty"
            }))
            return
        }

        // Submit login form
        val loginScript = """
            (function(){
            try {
                var formContainer = document.getElementById('page');
                if(!formContainer) return "Error: Form container not found";

                var usernameField = document.getElementById('id-username');
                var passwordField = document.getElementById('id-password');

                if (!usernameField) return "Error: Username field not found";
                if (!passwordField) return "Error: Password field not found";

                usernameField.value = '$username';
                passwordField.value = '$password';

                var form = formContainer.querySelector('form');
                if (!form) return "Error: Form not found";

                form.submit();
                return "Login submitted";
            } catch (e) {
                return "Error: " + e.message;
            }
            })();
        """.trimIndent()

        webView?.postDelayed({
            webView?.evaluateJavascript(loginScript) { result ->
                hasTriedFormSubmission = true
                if (result != "\"Login submitted\"") {
                    Logger.e(TAG, "Error submitting login form: $result")
                    callbacks.onLoad(Resource.failure(error = ResponseError.BAD_REQUEST.apply {
                        actualResponse = result
                    }))
                }
            }
        }, 2000)
    }

    private fun showOAuthPrompt(view: WebView?) {
        val allowAccessScript = """
(function () {
  const wait = setInterval(() => {
    const main = document.querySelector("#oauth-prompt");
    if (main) {
      clearInterval(wait);

      const toHide = [
        "nav.navbar",                 
        ".container > :not(#react-container)",
      ];
      toHide.forEach(sel => {
        document.querySelectorAll(sel).forEach(e => e.style.display = "none");
      });

      const containers = [
        "html", "body",
        ".container",
        "#react-container",
        "#oauth-prompt",
      ];

      containers.forEach(sel => {
        document.querySelectorAll(sel).forEach(e => {
          e.style.margin = "0";
          e.style.padding = "0";
        });
      });

      /* Keep everything at top-left */
      document.body.style.overflow = "hidden";
      document.body.style.margin = "0";
      document.body.style.padding = "16px";  
      const btn = document.querySelector(".btn-primary");
      if (btn) btn.style.marginLeft = "12px";

      console.log("Cleaned and aligned to top-left!");
    }
  }, 300);
})();

    """.trimIndent()

        view?.evaluateJavascript(allowAccessScript) {
            Logger.d(TAG, "Formatted OAuth prompt")
            callbacks.showOAuthAuthorizationPrompt()
            //Changing variable as this redirects to login page again
            hasTriedRedirectToLoginEndpoint = false
        }
    }

    private fun showGDPRPrompt(view: WebView?) {
        val allowAccessScript = """
(function () {
  const PERMA_HIDE = [
    // Navigation + Sidebars
    'nav',
    '#side-nav',
    '#side-nav-overlay',
    '.sidebar-nav',

    // Footer
    '.footer',

    // Toasts
    '.Toastify',

    // Music Player + Queue + Actions
    '[data-testid="brainzplayer"]',
    '[data-testid="brainzplayer-ui"]',
    '.music-player',
    '.player-buttons',
    '.player-buttons.secondary',
    '.queue',
    '#brainz-player',
    '.controls',
    '.actions',
    '.content',
    '.volume',
    '.progress-bar-wrapper',
    '.progress',
    '.progress-numbers',
    '.cover-art-scroll-wrapper'
  ];

  function hideEverything() {
    PERMA_HIDE.forEach(sel => {
      document.querySelectorAll(sel).forEach(e => {
        e.style.display = 'none';
        e.style.visibility = 'hidden';
        e.style.opacity = '0';
      });
    });
  }

  /* Initial cleanup */
  hideEverything();

  /* MutationObserver to block any new elements React inserts */
  const observer = new MutationObserver(() => hideEverything());
  observer.observe(document.body, { childList: true, subtree: true });

  const CLEAN_SELECTORS = [
    'html',
    'body',
    '#react-container',
    '.container-react',
    '.container-react-main',
    '[role="main"]'
  ];

  CLEAN_SELECTORS.forEach(sel => {
    document.querySelectorAll(sel).forEach(e => {
      e.style.margin = '0';
      e.style.padding = '0';
    });
  });

  document.body.style.padding = '16px';

  document.querySelectorAll('.well').forEach(w => {
    w.style.margin = '0';
    w.style.marginBottom = '12px';
    w.style.maxWidth = '600px';
  });

  /* Spacing above the submit button */
  const submitButton = document.querySelector('button[type="submit"]');
  if (submitButton) submitButton.style.marginTop = '4px';

  console.log("Clean GDPR-only mode + Player permanently blocked.");

})();
    """.trimIndent()

        view?.evaluateJavascript(allowAccessScript) {
            Logger.d(TAG, "Formatted GDPR prompt")
            callbacks.showGDPRConsentPrompt()
        }
    }


    private fun checkForEmailVerificationError(view: WebView?, noErrorLambda: () -> Unit) {
        val errorScript = """
        (function () {
          try {
            var errorElement = document.querySelector('.alert.alert-danger');
            return errorElement ? errorElement.textContent.trim() : null;
          } catch (e) {
            return "Error: " + e.message;
          }
        })();
    """.trimIndent()

        view?.evaluateJavascript(errorScript) { result ->
            if (result != "null" && result != null) {
                var errorMsg = result.removePrefix("\"").removeSuffix("\"")
                if (errorMsg.contains("verify the email before proceeding"))
                    errorMsg = "Email is not verified or already in use. Please check your inbox."
                Logger.e(TAG, "Error in logging in $errorMsg")
                callbacks.onLoad(Resource.failure(error = ResponseError.BAD_REQUEST.apply {
                    actualResponse = errorMsg
                }))
            } else {
                noErrorLambda()
            }
        }
    }
}