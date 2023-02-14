package org.listenbrainz.android.presentation.features.login

import CacheService
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.drawable.ColorDrawable
import android.net.Uri
import android.os.Bundle
import android.view.Menu
import android.view.View
import android.widget.*
import androidx.lifecycle.ViewModelProvider
import androidx.viewpager2.widget.ViewPager2
import com.tbuonomo.viewpagerdotsindicator.DotsIndicator
import dagger.hilt.android.AndroidEntryPoint
import org.listenbrainz.android.App
import org.listenbrainz.android.BuildConfig.APPLICATION_ID
import org.listenbrainz.android.R
import org.listenbrainz.android.data.di.Card
import org.listenbrainz.android.data.di.TotalListens
import org.listenbrainz.android.data.di.user_profile
import org.listenbrainz.android.data.sources.Constants
import org.listenbrainz.android.data.sources.Constants.PROFILE
import org.listenbrainz.android.data.sources.Constants.PROFILE_PIC
import org.listenbrainz.android.data.sources.Constants.RECENTLY_PLAYED_KEY
import org.listenbrainz.android.data.sources.api.ListenBrainzServiceGenerator
import org.listenbrainz.android.data.sources.api.entities.AccessToken
import org.listenbrainz.android.data.sources.api.entities.userdata.UserInfo
import org.listenbrainz.android.data.sources.brainzplayer.Playlist
import org.listenbrainz.android.data.sources.brainzplayer.Song
import org.listenbrainz.android.databinding.ActivityLoginBinding
import org.listenbrainz.android.presentation.features.adapters.CardAdapter
import org.listenbrainz.android.presentation.features.base.ListenBrainzActivity
import org.listenbrainz.android.presentation.features.dashboard.DashboardActivity
import org.listenbrainz.android.util.CircularPercentageChart
import org.listenbrainz.android.util.LBSharedPreferences
import org.listenbrainz.android.util.Log.d
import java.util.concurrent.TimeUnit
import org.listenbrainz.android.util.AppUsage

@AndroidEntryPoint
class LoginActivity : ListenBrainzActivity() {

    private var binding: ActivityLoginBinding? = null
    private var loginViewModel: LoginViewModel? = null
    var cache= App.context?.let { CacheService<Bitmap>(it, PROFILE_PIC) }
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
                setContentView(R.layout.activity_profile)
                val viewPager = findViewById<ViewPager2>(R.id.view_pager)
                val packageName = APPLICATION_ID
                val startTime = System.currentTimeMillis() - TimeUnit.DAYS.toMillis(10)
                val endTime = System.currentTimeMillis()
                val usageStats = AppUsage().getAppUsageTime(this, packageName, startTime, endTime)
                var totalListens=0
                val time=convertMillisToHoursAndMinutes(usageStats.foregroundTime)
                var cacheListen= App.context?.let { CacheService<TotalListens>(it, Constants.LAST_LISTEN) }
                var dataListen= cacheListen?.getData(TotalListens::class.java)
                if (dataListen != null) {
                    if (dataListen.isNotEmpty()) {
                        totalListens= dataListen[0].total

                    }
                }
                var cacheName= App.context?.let { CacheService<user_profile>(it, PROFILE) }
                val cards = listOf(
                    Card(time,"Average Time Spent",  R.drawable.ic_time),
                    Card(totalListens.toString(),"Music Listen",  R.drawable.ic_listen),
                    Card(Playlist.recentlyPlayed.favouriteArtist,"Favourite Artist", R.drawable.ic_artist_icon),
                    Card("2","Follower",  R.drawable.ic_user),
                    Card("3","Following ",  R.drawable.ic_artist)
                )
                val Name: TextView = findViewById(R.id.nameTextField)
                cacheName?.getData(user_profile::class.java)?.let {

                    if(it.isNotEmpty()) {

                        Name.text=it[0].name
                    }
                }
                var userName: String=""
                val adapter = CardAdapter(cards)
                viewPager.adapter = adapter
                val wormDotsIndicator = findViewById<DotsIndicator>(R.id.dots_indicator)
                wormDotsIndicator.attachTo(viewPager)
                val editButton: ImageView = findViewById(R.id.editAccountInfo)
                val Edit: EditText = findViewById(R.id.textField)
                val Save: ImageView = findViewById(R.id.save)
                editButton.setOnClickListener() {
                    Name.setVisibility(View.GONE);
                    Edit.setVisibility(View.VISIBLE);
                    Edit.requestFocus()
                    Save.setVisibility(View.VISIBLE);
                }
                Edit.setOnClickListener() {
                    Edit.requestFocus()
                }

                val user_name: TextView = findViewById(R.id.username)
                val email: TextView = findViewById(R.id.email)
                user_name.text = LBSharedPreferences.username
                email.text = LBSharedPreferences.email
                val chart = findViewById<CircularPercentageChart>(R.id.circular_percentage_chart)
                chart.setPercentage()
                chart.setOnClickListener {
                    val intent = Intent(Intent.ACTION_GET_CONTENT)
                    intent.type = "image/*"
                    startActivityForResult(intent, PICK_PHOTO_FOR_AVATAR)
                }
                var user: user_profile
                var data= cache?.getBitmap()
                if(data !=null) {
                   user = user_profile(Name.text.toString(), usageStats.foregroundTime, 1)
                }
                else
                {
                    user=user_profile(Name.text.toString(), usageStats.foregroundTime, 0)
                }
                if (user != null) {
                        cacheName?.saveData(user, user_profile::class.java, false)
                }
                Save.setOnClickListener() {
                    Name.setVisibility(View.VISIBLE);
                    Edit.setVisibility(View.GONE);
                    Save.setVisibility(View.GONE);
                    userName = Edit.getText().toString();
                    Name.setText(Edit.getText());
                    if(data !=null) {
                        user = user_profile(Edit.getText().toString(), usageStats.foregroundTime, 1)
                    }
                    else
                    {
                        user=user_profile(Edit.getText().toString(), usageStats.foregroundTime, 0)
                    }
                    if (user != null) {
                        cacheName?.saveData(user, user_profile::class.java, false)
                    }
                }
                val logout_btn: RelativeLayout = findViewById(R.id.logout_btn)
                logout_btn.setOnClickListener {
                    logoutUser()
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
        var cacheName= App.context?.let { CacheService<user_profile>(it, PROFILE) }
        var cacheRecent= App.context?.let { CacheService<Song>(it, RECENTLY_PLAYED_KEY) }
        var cacheListen= App.context?.let { CacheService<TotalListens>(it, Constants.LAST_LISTEN) }
        cacheRecent?.deleteData()
        cacheName?.deleteData()
        cacheListen?.deleteData()
        cache?.deleteData()
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

    fun convertMillisToHoursAndMinutes(millis: Long): String {
        val millis = millis / 7
        val hours = TimeUnit.MILLISECONDS.toHours(millis)
        val minutes = TimeUnit.MILLISECONDS.toMinutes(millis) % 60
        return "$hours h $minutes m"
    }


}