package org.listenbrainz.android.presentation.features.login

import CacheService
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.drawable.ColorDrawable
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.view.Menu
import android.widget.TextView
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.lifecycle.ViewModelProvider
import dagger.hilt.android.AndroidEntryPoint
import org.listenbrainz.android.App
import org.listenbrainz.android.R
import org.listenbrainz.android.data.sources.Constants.PROFILE_PIC
import org.listenbrainz.android.data.sources.api.ListenBrainzServiceGenerator
import org.listenbrainz.android.data.sources.api.entities.AccessToken
import org.listenbrainz.android.data.sources.api.entities.userdata.UserInfo
import org.listenbrainz.android.databinding.ActivityLoginBinding
import org.listenbrainz.android.presentation.features.base.ListenBrainzActivity
import org.listenbrainz.android.presentation.features.dashboard.DashboardActivity
import org.listenbrainz.android.util.CircularPercentageChart
import org.listenbrainz.android.util.LBSharedPreferences
import org.listenbrainz.android.util.Log.d


@AndroidEntryPoint
class LoginActivity : ListenBrainzActivity() {

    private var binding: ActivityLoginBinding? = null
    private var loginViewModel: LoginViewModel? = null
    val PICK_PHOTO_FOR_AVATAR= 0
    public override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding!!.root)
        supportActionBar!!.setBackgroundDrawable(ColorDrawable(resources.getColor(R.color.app_bg)))
        supportActionBar!!.setDisplayHomeAsUpEnabled(true)
        loginViewModel = ViewModelProvider(this)[LoginViewModel::class.java]
        loginViewModel!!.accessTokenLiveData!!.observe(this) { accessToken: AccessToken? ->
            saveOAuthToken(accessToken)
        }
        loginViewModel!!.userInfoLiveData!!.observe(this) { userInfo: UserInfo? ->
            saveUserInfo(userInfo)
        }
        when (LBSharedPreferences.loginStatus) {
            LBSharedPreferences.STATUS_LOGGED_IN -> {
                println(LBSharedPreferences.loginStatus)
                setContentView(R.layout.activity_profile)
                val user_name: TextView = findViewById(R.id.username)
                val email: TextView = findViewById(R.id.email)
                user_name.text = LBSharedPreferences.username
                email.text = LBSharedPreferences.email
                val chart = findViewById<CircularPercentageChart>(R.id.circular_percentage_chart)
                chart.setPercentage(70f)
                chart.setOnClickListener {
                    val intent = Intent(Intent.ACTION_GET_CONTENT)
                    intent.type = "image/*"
                    startActivityForResult(intent, PICK_PHOTO_FOR_AVATAR)
                }
            }
            else -> binding!!.loginBtn.setOnClickListener { startLogin() }
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == PICK_PHOTO_FOR_AVATAR && resultCode == RESULT_OK) {
            if (data == null || data.data == null) {
                return
            }
            try {
                val imageStream = contentResolver.openInputStream(data.data!!)
                val selectedImage = BitmapFactory.decodeStream(imageStream)
                var cache= App.context?.let { CacheService<Bitmap>(it, PROFILE_PIC) }
                cache?.saveBitmap(selectedImage)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }
    override fun onResume() {
        val callbackUri = intent.data
        if (callbackUri != null && callbackUri.toString().startsWith(ListenBrainzServiceGenerator.OAUTH_REDIRECT_URI)) {
            val code = callbackUri.getQueryParameter("code")
            if (code != null) loginViewModel!!.fetchAccessToken(code)
        }
        super.onResume()
    }

    private fun startLogin() {
        val intent = Intent(
                Intent.ACTION_VIEW,
                Uri.parse(ListenBrainzServiceGenerator.AUTH_BASE_URL
                        + "authorize"
                        + "?response_type=code"
                        + "&client_id=" + ListenBrainzServiceGenerator.CLIENT_ID
                        + "&redirect_uri=" + ListenBrainzServiceGenerator.OAUTH_REDIRECT_URI
                        + "&scope=profile%20collection%20tag%20rating%20email"))
        startActivity(intent)
    }

    private fun saveOAuthToken(accessToken: AccessToken?) {
        when {
            accessToken != null -> {
                d(accessToken.accessToken)
                LBSharedPreferences.saveOAuthToken(accessToken)
                loginViewModel!!.fetchUserInfo()
            }
            else -> {
                Toast.makeText(applicationContext,
                    "Failed to obtain access token ",
                    Toast.LENGTH_LONG).show()
            }
        }
    }

    private fun saveUserInfo(userInfo: UserInfo?) {
        if (userInfo != null &&
                LBSharedPreferences.loginStatus == LBSharedPreferences.STATUS_LOGGED_OUT) {
            LBSharedPreferences.saveUserInfo(userInfo)
            Toast.makeText(applicationContext,
                    "Login successful. " + userInfo.username + " is now logged in.",
                    Toast.LENGTH_LONG).show()
            startActivity(Intent(this, DashboardActivity::class.java))
            d(userInfo.username)
            finish()
        }
    }

    private fun logoutUser() {
        LBSharedPreferences.logoutUser()
        Toast.makeText(applicationContext,
                "User has successfully logged out.",
                Toast.LENGTH_LONG).show()
        startActivity(Intent(this, DashboardActivity::class.java))
        finish()
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        super.onCreateOptionsMenu(menu)
        menu.findItem(R.id.menu_open_website).isVisible = false
        return true
    }

}