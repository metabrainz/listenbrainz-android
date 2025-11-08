package org.listenbrainz.android.ui.screens.profile

import android.graphics.Bitmap
import android.os.Build
import android.webkit.WebResourceError
import android.webkit.WebResourceRequest
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.core.net.toUri
import com.limurse.logger.Logger
import org.listenbrainz.android.model.ResponseError
import org.listenbrainz.android.util.Resource

private const val TAG = "CreateAccountWebClient"

class CreateAccountWebClient(
    val username: String,
    val email: String,
    val password: String,
    val confirmPassword: String,
    private val onLoad: (Resource<String>) -> Unit,
    private val onPageLoadStateChange: (Boolean, String?) -> Unit,
) : WebViewClient() {

    // Track account creation flow state
    private var isCaptaVerified: Boolean = false
    private var hasTriedFormSubmission = false
    private var isAccountCreationFailed = false
    private var currentPage: String? = null

    override fun onPageStarted(view: WebView?, url: String?, favicon: Bitmap?) {
        super.onPageStarted(view, url, favicon)

        url?.let {
            val uri = it.toUri()
            currentPage = when {
                uri.host == "musicbrainz.org" && uri.path == "/register" ->
                    "Connecting to MusicBrainz registration..."

                uri.host == "listenbrainz.org" ->
                    "Redirecting to ListenBrainz..."

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

    }

    override fun onPageFinished(view: WebView?, url: String?) {
        super.onPageFinished(view, url)
        onPageLoadStateChange(false, null)

        if (url == null) {
            onLoad(Resource.failure(error = ResponseError.BAD_REQUEST.apply {
                actualResponse = "URL is null, cannot proceed with account creation"
            }))
            return
        }

        val uri = url.toUri()
        Logger.d(TAG, "Page loaded: ${uri.host}${uri.path}")

        when {
            uri.host == "musicbrainz.org" && uri.path == "/register" && !isCaptaVerified -> {
                Logger.d(TAG, "Hiding other elements except captcha")
                if(view != null) {
                    hideOtherElementsExceptCaptcha(view)
                }
//                isCaptaVerified = true
            }
            uri.host == "musicbrainz.org" && uri.path == "/register" && hasTriedFormSubmission -> {
                checkForRegistrationErrors(view)
            }

            !hasTriedFormSubmission && uri.host == "musicbrainz.org" && uri.path == "/register" && !isAccountCreationFailed -> {
                hasTriedFormSubmission = true
                Logger.d(TAG, "Submitting registration form")
                onLoad(Resource.loading())
                if (view != null) {
                    submitRegistrationForm(view)
                } else {
                    onLoad(Resource.failure(error = ResponseError.BAD_REQUEST.apply {
                        actualResponse = "WebView is null, cannot submit registration form"
                    }))
                }
            }

            uri.host == "listenbrainz.org" -> {
                Logger.d(
                    TAG,
                    "Successfully redirected to ListenBrainz - account creation successful"
                )
                onLoad(Resource.success("Account created successfully! Please check your inbox to verify your email address."))
            }
        }
    }

    private fun checkForRegistrationErrors(view: WebView?) {
        view?.evaluateJavascript(
            "(function() { return document.querySelector('.error') ? document.querySelector('.error').textContent : null; })()"
        ) { errorResult ->
            if (errorResult != "null" && errorResult != null) {
                val errorMsg = errorResult.replace("\"", "").trim()
                Logger.e(TAG, "Registration failed: $errorMsg")
                isAccountCreationFailed = true

                onLoad(Resource.failure(error = ResponseError.BAD_REQUEST.apply {
                    actualResponse = errorMsg
                }))
            } else {
                // Check for .errors element if .error is not found
                view.evaluateJavascript(
                    "(function() { return document.querySelector('.errors') ? document.querySelector('.errors').textContent : null; })()"
                ) { errorsResult ->
                    if (errorsResult != "null" && errorsResult != null) {
                        val errorMsg = errorsResult.replace("\"", "").trim()
                        Logger.e(TAG, "Registration failed: $errorMsg")
                        isAccountCreationFailed = true

                        onLoad(Resource.failure(error = ResponseError.BAD_REQUEST.apply {
                            actualResponse = errorMsg
                        }))
                    }
                }
            }
        }
    }

    private fun submitRegistrationForm(view: WebView) {
        if (username.isEmpty() || email.isEmpty() || password.isEmpty() || confirmPassword.isEmpty()) {
            onLoad(Resource.failure(error = ResponseError.BAD_REQUEST.apply {
                actualResponse = "All fields are required"
            }))
            return
        }

        // Submit registration form with the provided JavaScript
        val registrationScript = """
            (function () {
                try {
                    var formContainer = document.getElementById('page');
                    if (!formContainer) return "Error: Form container not found";

                    var usernameField = document.getElementById('id-register.username');
                    var emailField = document.getElementById('id-register.email');
                    var passwordField = document.getElementById('id-register.password');
                    var confirmPasswordField = document.getElementById('id-register.confirm_password');

                    if (!usernameField) return "Error: Username field not found";
                    if (!emailField) return "Error: Email field not found";
                    if (!passwordField) return "Error: Password field not found";
                    if (!confirmPasswordField) return "Error: Confirm Password field not found";

                    usernameField.value = '$username';
                    emailField.value = '$email';
                    passwordField.value = '$password';
                    confirmPasswordField.value = '$confirmPassword';

                    var form = formContainer.querySelector('form');
                    if (!form) return "Error: Form not found";

                    form.submit();
                    return "Registration submitted";
                } catch (e) {
                    return "Error: " + e.message;
                }
            })();
        """.trimIndent()

        view.postDelayed({
            view.evaluateJavascript(registrationScript) { result ->
                if (result != "\"Registration submitted\"") {
                    Logger.e(TAG, "Error submitting registration form: $result")
                    onLoad(Resource.failure(error = ResponseError.BAD_REQUEST.apply {
                        actualResponse = result
                    }))
                }
            }
        }, 2000)
    }

    private fun hideOtherElementsExceptCaptcha(view: WebView) {
        val script = """
(function() {
  const waitForCaptcha = setInterval(() => {
    const captcha = document.querySelector('.mtcaptcha');
    if (captcha) {
      clearInterval(waitForCaptcha);

      // Hide header/footer and unrelated rows
      const toHide = [
        '.header', '#footer', 
        '.fullwidth > p', '.fullwidth > h1',
        '.register-form > form > .row:not(:has(.mtcaptcha))'
      ];
      toHide.forEach(sel => {
        document.querySelectorAll(sel).forEach(e => e.style.display = 'none');
      });

      // Remove spacing from containers
      const containers = ['html', 'body', '#page', '.fullwidth', '.register-form', 'form', '.row'];
      containers.forEach(sel => {
        document.querySelectorAll(sel).forEach(e => {
          e.style.margin = '0';
          e.style.padding = '0';
        });
      });

      // Remove spacing from body
      document.body.style.overflow = 'hidden';
      document.body.style.padding = '0';
      document.body.style.margin = '0';

      // Add small left padding to captcha
      captcha.style.paddingLeft = '8px';
    }
  }, 500);
})();

        """.trimIndent()

        view.postDelayed({
            view.evaluateJavascript(script) { result ->

            }
        }, 2000)
    }
}
