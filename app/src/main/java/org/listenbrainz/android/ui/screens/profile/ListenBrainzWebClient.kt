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

private const val TAG = "ListenBrainzWebClient"

class ListenBrainzWebClient(
    val username: String,
    val password: String,
    private val onLoad: (Resource<String>) -> Unit,
    private val onPageLoadStateChange: (Boolean, String?) -> Unit,
) : WebViewClient() {

    // Track auth flow state
    private var hasTriedFormSubmission = false
    private var hasTriedRedirectToLoginEndpoint = false
    private var hasTriedSettingsNavigation = false
    private var isLoginFailed = false
    private var currentPage: String? = null
    private var hasTriedOAuthAuthentication = false

    override fun onPageStarted(view: WebView?, url: String?, favicon: Bitmap?) {
        super.onPageStarted(view, url, favicon)

        // Update UI with current page info
        url?.let {
            val uri = it.toUri()
            currentPage = when {
                uri.host == "musicbrainz.org" && uri.path == "/login" ->
                    "Connecting to MusicBrainz..."

                uri.host == "listenbrainz.org" && uri.path == "/login" ->
                    "Connecting to ListenBrainz..."

                uri.host == "listenbrainz.org" && uri.path == "/login/musicbrainz" ->
                    "Authenticating with ListenBrainz..."

                uri.host == "listenbrainz.org" && uri.path?.contains("/settings") == true ->
                    "Retrieving authentication token..."

                else -> "Loading page..."
            }
            onPageLoadStateChange(true, currentPage)
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
        onPageLoadStateChange(false, null)

        if (url == null) {
            onLoad(Resource.failure(error = ResponseError.BAD_REQUEST.apply {
                actualResponse = "URL is null, cannot proceed with login"
            }))
            return
        }

        val uri = url.toUri()
        Logger.d(TAG, "Page loaded: ${uri.host}${uri.path}")

        when {
            // Check for login errors on MusicBrainz login page
            uri.host == "musicbrainz.org" && uri.path == "/login" && hasTriedFormSubmission -> {
                checkForLoginErrors(view)
            }

            // Submit login form on MusicBrainz login page
            !hasTriedFormSubmission && uri.host == "musicbrainz.org" && uri.path == "/login" && !isLoginFailed -> {
                hasTriedFormSubmission = true
                Logger.d(TAG, "Submitting login form")
                onLoad(Resource.loading())
                if (view != null) {
                    submitLoginForm(view)
                } else {
                    onLoad(Resource.failure(error = ResponseError.BAD_REQUEST.apply {
                        actualResponse = "WebView is null, cannot submit login form"
                    }))
                }
            }

            //Edge case 1: User's account is not linked with ListenBrainz
            uri.path?.contains("new-oauth2/authorize") == true && !hasTriedOAuthAuthentication && !hasTriedSettingsNavigation -> {
                Logger.e(TAG, "Account not linked with ListenBrainz, running script to accept")
                hasTriedOAuthAuthentication = true
                view?.postDelayed({
                    allowAccess(view)
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

                onLoad(Resource.failure(error = ResponseError.BAD_REQUEST.apply {
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
                view?.postDelayed(2000) {
                    checkForEmailVerificationError(view) {
                        view.loadUrl("https://listenbrainz.org/settings")
                    }
                }
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
                onLoad(Resource.loading())
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
                onLoad(Resource.success(token))
            } else {
                Logger.e(TAG, "Auth token not found")
                onLoad(Resource.failure(error = ResponseError.BAD_REQUEST.apply {
                    actualResponse = "Could not retrieve authentication token"
                }))
            }
        }
    }

    private fun submitLoginForm(view: WebView) {
        // Validate inputs
        if (username.isEmpty() || password.isEmpty()) {
            onLoad(Resource.failure(error = ResponseError.BAD_REQUEST.apply {
                actualResponse = "Username and password cannot be empty"
            }))
            return
        }

        val usernameEscaped = JSONObject.quote(username);
        val passwordEscaped = JSONObject.quote(password);

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

        view.postDelayed({
            view.evaluateJavascript(loginScript) { result ->
                if (result != "\"Login submitted\"") {
                    Logger.e(TAG, "Error submitting login form: $result")
                    onLoad(Resource.failure(error = ResponseError.BAD_REQUEST.apply {
                        actualResponse = result
                    }))
                }
            }
        }, 2000)
    }

    private fun allowAccess(view: WebView?) {
        val allowAccessScript = """
        (function () {
  try {
    var attempts = 0;

    function tryClick() {
      var formContainer = document.getElementById('react-container');
      if (!formContainer) return "Error: Form container not found";

      var button = formContainer.querySelector('button.btn.btn-primary');
      if (button) {
        button.click();
        return "Allow access clicked";
      }

      if (attempts < 20) { // wait up to ~2 seconds
        attempts++;
        setTimeout(tryClick, 100);
      }
    }

    tryClick();
    return "Waiting for Allow access button...";
  } catch (e) {
    return "Error: " + e.message;
  }
})();

    """.trimIndent()

        view?.evaluateJavascript(allowAccessScript) { result ->
            Logger.d(TAG, "Allow access result: $result")
            //Changing variable as this redirects to login page again
            hasTriedRedirectToLoginEndpoint = false
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
                onLoad(Resource.failure(error = ResponseError.BAD_REQUEST.apply {
                    actualResponse = errorMsg
                }))
            } else {
                noErrorLambda()
            }
        }
    }
}
