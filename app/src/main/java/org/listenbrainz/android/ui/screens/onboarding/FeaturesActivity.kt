package org.listenbrainz.android.ui.screens.onboarding

import android.Manifest
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.core.content.ContextCompat
import androidx.core.view.WindowCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.limurse.onboard.OnboardAdvanced
import com.limurse.onboard.OnboardFragment
import com.limurse.onboard.OnboardPageTransformerType
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import org.listenbrainz.android.R
import org.listenbrainz.android.repository.preferences.AppPreferences
import org.listenbrainz.android.ui.screens.main.MainActivity
import org.listenbrainz.android.ui.screens.profile.LoginActivity
import org.listenbrainz.android.util.Log
import org.listenbrainz.android.viewmodel.FeaturesViewModel
import javax.inject.Inject

@AndroidEntryPoint
class FeaturesActivity : OnboardAdvanced() {
    @Inject
    lateinit var appPreferences: AppPreferences
    private val featuresViewModel: FeaturesViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        // Disable for android 15
        theme.applyStyle(R.style.OptOutEdgeToEdgeEnforcement, /* force */ false)
        super.onCreate(savedInstanceState)

        showSignInButton = true
        isWizardMode = true

        showStatusBar(true)
        setStatusBarColorRes(R.color.app_bg)
        setNavBarColorRes(R.color.app_bg)

        askForPermissions(
            when {
                Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE -> arrayOf(
                    Manifest.permission.POST_NOTIFICATIONS,
                    Manifest.permission.READ_MEDIA_AUDIO,
                )
                Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU -> arrayOf(
                    Manifest.permission.POST_NOTIFICATIONS,
                    Manifest.permission.READ_MEDIA_AUDIO,
                )
                Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q -> arrayOf(
                    Manifest.permission.READ_EXTERNAL_STORAGE
                )
                else -> arrayOf(
                    Manifest.permission.READ_EXTERNAL_STORAGE,
                    Manifest.permission.WRITE_EXTERNAL_STORAGE
                )
            },
            slideNumber = 1,
            required = true
        )

        addSlides()

        setTransformer(OnboardPageTransformerType.Parallax())
    }

    private fun addSlides() {
        val slides = listOf(
            SlideData("Listens", "Track your music listening habits", R.raw.teen),
            SlideData("Critiques", "Read and write about an album or event", R.raw.review),
            SlideData("BrainzPlayer", "Listen to locally saved music", R.raw.music_player)
        )

        slides.forEach { slide ->
            addSlide(
                OnboardFragment.newInstance(
                    slide.title,
                    slide.description,
                    resourceId = slide.resourceId,
                    backgroundColor = ContextCompat.getColor(applicationContext, R.color.app_bg),
                    titleColor = ContextCompat.getColor(applicationContext, R.color.text),
                    descriptionColor = ContextCompat.getColor(applicationContext, R.color.text),
                    isLottie = true
                )
            )
        }
    }

    override fun onDonePressed(currentFragment: Fragment?) {
        super.onDonePressed(currentFragment)
        Log.d("Onboarding completed")
        appPreferences.onboardingCompleted = true
        startActivity(Intent(this, MainActivity::class.java))
        finish()
    }

    override fun onNextPressed(currentFragment: Fragment?) {
        if (!appPreferences.isNotificationServiceAllowed) {
            Toast.makeText(this, "Allow notification access to submit listens", Toast.LENGTH_SHORT).show()
            val intent = Intent(Settings.ACTION_NOTIFICATION_LISTENER_SETTINGS)
            startActivity(intent)
        } else {
            super.onNextPressed(currentFragment)
        }
    }

    override fun onSignInPressed(currentFragment: Fragment?) {
        super.onSignInPressed(currentFragment)
        featuresViewModel.appPreferences.onboardingCompleted = true
        startActivity(Intent(this, LoginActivity::class.java))
    }

    override fun onResume() {
        super.onResume()
        lifecycleScope.launch {
            if (featuresViewModel.appPreferences.onboardingCompleted && featuresViewModel.appPreferences.isUserLoggedIn()) {
                startActivity(Intent(this@FeaturesActivity, MainActivity::class.java))
                finish()
            }
        }
    }

    data class SlideData(val title: String, val description: String, val resourceId: Int)
}