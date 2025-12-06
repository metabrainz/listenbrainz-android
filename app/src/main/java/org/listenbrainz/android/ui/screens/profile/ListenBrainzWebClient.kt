package org.listenbrainz.android.ui.screens.profile

import android.graphics.Bitmap
import android.net.Uri
import android.os.Build
import android.webkit.WebResourceError
import android.webkit.WebResourceRequest
import android.webkit.WebView
import android.webkit.WebViewClient
import org.json.JSONObject
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

    companion object {
        private const val SCRIPT_CHECK_LOGIN_ERROR = """
            (function() { 
                return document.querySelector('.error') ? document.querySelector('.error').textContent : null; 
            })()
        """

        private const val SCRIPT_EXTRACT_TOKEN = """
            (function() { 
                return document.getElementById('auth-token') ? document.getElementById('auth-token').value : 'not found'; 
            })();
        """

        private const val SCRIPT_CHECK_EMAIL_VERIFICATION_ERROR = """
            (function () {
              try {
                var errorElement = document.querySelector('.alert.alert-danger');
                return errorElement ? errorElement.textContent.trim() : null;
              } catch (e) {
                return "Error: " + e.message;
              }
            })();
        """

        private const val SCRIPT_OAUTH_PROMPT_FORMATTING = """
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

                  document.body.style.overflow = "hidden";
                  document.body.style.margin = "0";
                  document.body.style.padding = "16px";  
                  const btn = document.querySelector(".btn-primary");
                  if (btn) btn.style.marginLeft = "12px";

                  console.log("Cleaned and aligned to top-left!");
                }
              }, 300);
            })();
        """

        private const val SCRIPT_GDPR_PROMPT_FORMATTING = """
            (function () {
              const PERMA_HIDE = [
                'nav',
                '#side-nav',
                '#side-nav-overlay',
                '.sidebar-nav',
                '.footer',
                '.Toastify',
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
            
              hideEverything();
            
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
            
              const submitButton = document.querySelector('button[type="submit"]');
              if (submitButton) submitButton.style.marginTop = '4px';
            })();
        """

        private const val SCRIPT_OAUTH_LISTENER = """
            (function () {
              function setupOAuthListeners() {
                const allowBtn = document.querySelector('button[type="submit"]');
                const cancelBtn = document.querySelector('a.btn.btn-default');

                if (!allowBtn && !cancelBtn) return;

                if (allowBtn) {
                  allowBtn.addEventListener('click', function () {
                    if (window.AndroidInterface) {
                      window.AndroidInterface.onAllowClicked();
                    }
                  });
                }

                if (cancelBtn) {
                  cancelBtn.addEventListener('click', function () {
                    if (window.AndroidInterface) {
                      window.AndroidInterface.onCancelClicked();
                    }
                  });
                }
              }

              setupOAuthListeners();

              const observer = new MutationObserver(setupOAuthListeners);
              observer.observe(document.body, { childList: true, subtree: true });
            })();
        """

        private const val SCRIPT_GDPR_LISTENER = """
            (function () {
              document.addEventListener("click", function (e) {
                const t = e.target;

                if (t.matches('#gdpr-agree')) {
                  window.AndroidInterface?.onGDPRSuccess();
                }

                if (t.matches('#gdpr-disagree')) {
                  window.AndroidInterface?.onGDPRDelete();
                }

                if (t.matches('button[type="submit"]')) {
                  const agree = document.querySelector('#gdpr-agree')?.checked;
                  const disagree = document.querySelector('#gdpr-disagree')?.checked;

                  if (agree) {
                    window.AndroidInterface?.onGDPRSubmitSuccess();
                  } else if (disagree) {
                    window.AndroidInterface?.onGDPRSubmitDelete();
                  }
                }
              });
            })();
        """

        fun getLoginFormSubmissionScript(username: String, password: String): String {
            val usernameEscaped = JSONObject.quote(username)
            val passwordEscaped = JSONObject.quote(password)

            return """
            (function(){
            try {
                var formContainer = document.getElementById('page');
                if(!formContainer) return "Error: Form container not found";

                var usernameField = document.getElementById('id-username');
                var passwordField = document.getElementById('id-password');

                if (!usernameField) return "Error: Username field not found";
                if (!passwordField) return "Error: Password field not found";

                usernameField.value = $usernameEscaped;
                passwordField.value = $passwordEscaped;

                var form = formContainer.querySelector('form');
                if (!form) return "Error: Form not found";

                form.submit();
                return "Login submitted";
            } catch (e) {
                return "Error: " + e.message;
            }
            })();
        """.trimIndent()
        }
    }

    // Track auth flow state
    private var hasLoginFormReadyCallbackBeenMade = false
    private var hasTriedFormSubmission = false
    private var hasTriedRedirectToLoginEndpoint = false
    private var hasTriedSettingsNavigation = false
    private var isLoginFailed = false
    private var hasTriedOAuthAuthentication = false
    private var webView: WebView? = null

    override fun onPageStarted(view: WebView?, url: String?, favicon: Bitmap?) {
        super.onPageStarted(view, url, favicon)

        // Update UI with current page info
        url?.let {
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
        if(view != null) {
            webView = view
        }

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
        if (view == null) {
            Logger.e(TAG, "WebView is null, cannot check for login errors")
            return
        }

        view.evaluateJavascript(SCRIPT_CHECK_LOGIN_ERROR) { result ->
            if (result != null && result != "null" && result.isNotEmpty()) {
                val errorMsg = result.replace("\"", "").trim()
                if (errorMsg.isNotEmpty()) {
                    Logger.e(TAG, "Login failed: $errorMsg")
                    isLoginFailed = true

                    callbacks.onLoad(Resource.failure(error = ResponseError.BAD_REQUEST.apply {
                        actualResponse = errorMsg
                    }))
                }
            }
        }
    }

    private fun handleListenBrainzNavigation(view: WebView?, uri: Uri) {
         when {
            // Step 1: Redirect to login endpoint
            uri.path?.endsWith("login/") == true && !hasTriedRedirectToLoginEndpoint-> {
                Logger.d(TAG, "Redirecting to login endpoint from $uri")
                hasTriedRedirectToLoginEndpoint = true
                view?.postDelayed(1000) {
                    view.loadUrl("https://listenbrainz.org/login/musicbrainz")
                    hasTriedSettingsNavigation = false
                }
            }

            // Edge case 2: Data protection terms not accepted. This is not a blocker for login, so we
            // can skip this step.
            uri.path?.contains("agree-to-terms") == true -> {
                showGDPRPrompt(view)
            }

            // Step 2: Navigate to settings to get token with edge case 2
            !hasTriedSettingsNavigation -> {
                Logger.d(TAG, "Navigating to settings page to extract token")
                navigateToSettings(view)
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

    fun navigateToSettings(view: WebView?) {
        val view = view?: webView
        if (view == null) {
            Logger.e(TAG, "WebView is null, cannot navigate to settings")
            return
        }


        hasTriedSettingsNavigation = true
        view.postDelayed(1000) {
            checkForEmailVerificationError(view){}
            view.loadUrl("https://listenbrainz.org/settings")
        }
    }

    private fun extractToken(view: WebView?) {
        if (view == null) {
            Logger.e(TAG, "WebView is null, cannot extract token")
            callbacks.onLoad(Resource.failure(error = ResponseError.BAD_REQUEST.apply {
                actualResponse = "WebView is not available"
            }))
            return
        }

        try {
            view.evaluateJavascript(SCRIPT_EXTRACT_TOKEN) { value ->
                if (value == null) {
                    Logger.e(TAG, "Auth token extraction returned null")
                    callbacks.onLoad(Resource.failure(error = ResponseError.BAD_REQUEST.apply {
                        actualResponse = "Could not retrieve authentication token"
                    }))
                    return@evaluateJavascript
                }

                val token = value.removePrefix("\"").removeSuffix("\"").trim()
                if (token.isNotEmpty() && token != "not found" && token != "null") {
                    Logger.d(TAG, "Auth token found")
                    callbacks.onLoad(Resource.success(token))
                } else {
                    Logger.e(TAG, "Auth token not found or invalid: $token")
                    callbacks.onLoad(Resource.failure(error = ResponseError.BAD_REQUEST.apply {
                        actualResponse = "Could not retrieve authentication token. Please ensure you're logged in."
                    }))
                }
            }
        } catch (e: Exception) {
            Logger.e(TAG, "Exception while extracting token: ${e.message}")
            callbacks.onLoad(Resource.failure(error = ResponseError.BAD_REQUEST.apply {
                actualResponse = "Failed to extract token: ${e.message}"
            }))
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

        // Check if webView is available
        val currentWebView = webView
        if (currentWebView == null) {
            Logger.e(TAG, "WebView is null, cannot submit login form")
            callbacks.onLoad(Resource.failure(error = ResponseError.BAD_REQUEST.apply {
                actualResponse = "Login form is not ready. Please try again."
            }))
            return
        }

        // Submit login form
        currentWebView.postDelayed(2000) {
            try {
                currentWebView.evaluateJavascript(getLoginFormSubmissionScript(username, password)) { result ->
                    hasTriedFormSubmission = true
                    if (result != "\"Login submitted\"") {
                        val errorMsg = result?.replace("\"", "")?.trim() ?: "Unknown error during form submission"
                        Logger.e(TAG, "Error submitting login form: $errorMsg")
                        callbacks.onLoad(Resource.failure(error = ResponseError.BAD_REQUEST.apply {
                            actualResponse = errorMsg
                        }))
                    } else {
                        Logger.d(TAG, "Login form submitted successfully")
                    }
                }
            } catch (e: Exception) {
                Logger.e(TAG, "Exception while submitting login form: ${e.message}")
                callbacks.onLoad(Resource.failure(error = ResponseError.BAD_REQUEST.apply {
                    actualResponse = "Failed to submit login form: ${e.message}"
                }))
            }
        }
    }

    private fun showOAuthPrompt(view: WebView?) {
        if (view == null) {
            Logger.e(TAG, "WebView is null, cannot show OAuth prompt")
            return
        }

        try {
            view.evaluateJavascript(SCRIPT_OAUTH_PROMPT_FORMATTING) {
                Logger.d(TAG, "Formatted OAuth prompt")
                callbacks.showOAuthAuthorizationPrompt()
                //Changing variable as this redirects to login page again
                hasTriedRedirectToLoginEndpoint = false
                hasTriedSettingsNavigation = false
            }
        } catch (e: Exception) {
            Logger.e(TAG, "Exception while showing OAuth prompt: ${e.message}")
        }

        setupOAuthListener(view)
    }

    private fun showGDPRPrompt(view: WebView?) {
        if (view == null) {
            Logger.e(TAG, "WebView is null, cannot show GDPR prompt")
            return
        }

        try {
            view.evaluateJavascript(SCRIPT_GDPR_PROMPT_FORMATTING) {
                setupGDPRListener(view)
                Logger.d(TAG, "Formatted GDPR prompt")
                callbacks.showGDPRConsentPrompt()
                //Setting up listener
            }
        } catch (e: Exception) {
            Logger.e(TAG, "Exception while showing GDPR prompt: ${e.message}")
        }
    }


    private fun checkForEmailVerificationError(view: WebView?, noErrorLambda: () -> Unit) {
        if (view == null) {
            Logger.e(TAG, "WebView is null, cannot check for email verification error")
            callbacks.onLoad(Resource.failure(error = ResponseError.BAD_REQUEST.apply {
                actualResponse = "WebView is not available"
            }))
            return
        }

        try {
            view.evaluateJavascript(SCRIPT_CHECK_EMAIL_VERIFICATION_ERROR) { result ->
                if (result != null && result != "null" && result.isNotEmpty()) {
                    var errorMsg = result.removePrefix("\"").removeSuffix("\"").trim()
                    if (errorMsg.isNotEmpty()) {
                        if (errorMsg.contains("verify the email before proceeding"))
                            errorMsg = "Email is not verified or already in use. Please check your inbox."
                        Logger.e(TAG, "Error in logging in: $errorMsg")
                        callbacks.onLoad(Resource.failure(error = ResponseError.BAD_REQUEST.apply {
                            actualResponse = errorMsg
                        }))
                    } else {
                        noErrorLambda()
                    }
                } else {
                    noErrorLambda()
                }
            }
        } catch (e: Exception) {
            Logger.e(TAG, "Exception while checking email verification error: ${e.message}")
            noErrorLambda()
        }
    }

    fun setupOAuthListener(view: WebView){
        view.postDelayed({
            view.evaluateJavascript(SCRIPT_OAUTH_LISTENER, null)
        }, 2000)
    }

    fun setupGDPRListener(view: WebView){
        view.postDelayed({
            view.evaluateJavascript(SCRIPT_GDPR_LISTENER, null)
        }, 2000)
    }

}
