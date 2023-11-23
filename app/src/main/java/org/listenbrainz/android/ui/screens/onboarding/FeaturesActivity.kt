package org.listenbrainz.android.ui.screens.onboarding

import android.Manifest
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.widget.Toast
import androidx.activity.viewModels
import androidx.core.content.ContextCompat
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
import org.listenbrainz.android.util.Log.d
import org.listenbrainz.android.viewmodel.FeaturesViewModel
import javax.inject.Inject

@AndroidEntryPoint
class FeaturesActivity : OnboardAdvanced() {
    @Inject
    lateinit var appPreferences: AppPreferences
    private val featuresViewModel: FeaturesViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        showSignInButton = true
        isWizardMode = true

        showStatusBar(true)
        setStatusBarColorRes(R.color.app_bg)

        when {
            Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU -> {
                askForPermissions(
                    permissions = arrayOf(Manifest.permission.POST_NOTIFICATIONS,
                        Manifest.permission.READ_MEDIA_IMAGES, Manifest.permission.READ_MEDIA_AUDIO
                    ),
                    slideNumber = 1,
                    required = true
                )
            }
            Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q -> {
                askForPermissions(
                    permissions = arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE),
                    slideNumber = 1,
                    required = true
                )
            }
            else -> {
                askForPermissions(
                    permissions = arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.WRITE_EXTERNAL_STORAGE),
                    slideNumber = 1,
                    required = true
                )
            }
        }

        addSlide(
            OnboardFragment.newInstance(
                "Listens",
                "Track your music listening habits ",
                resourceId = R.raw.teen,
                backgroundColor =  ContextCompat.getColor(applicationContext, R.color.app_bg),
                titleColor = ContextCompat.getColor(applicationContext, R.color.text),
                descriptionColor = ContextCompat.getColor(applicationContext, R.color.text),
                isLottie = true
            )
        )

        addSlide(
            OnboardFragment.newInstance(
                "Critiques",
                "Read and write about an album or event",
                resourceId = R.raw.review,
                backgroundColor =  ContextCompat.getColor(applicationContext, R.color.app_bg),
                titleColor = ContextCompat.getColor(applicationContext, R.color.text),
                descriptionColor = ContextCompat.getColor(applicationContext, R.color.text),
                isLottie = true
            )
        )

        addSlide(
            OnboardFragment.newInstance(
                "BrainzPlayer",
                "Listen to locally saved music",
                resourceId = R.raw.music_player,
                backgroundColor =  ContextCompat.getColor(applicationContext, R.color.app_bg),
                titleColor = ContextCompat.getColor(applicationContext, R.color.text),
                descriptionColor = ContextCompat.getColor(applicationContext, R.color.text),
                isLottie = true
            )
        )

        setTransformer(OnboardPageTransformerType.Parallax())
    }

    override fun onDonePressed(currentFragment: Fragment?) {
        super.onDonePressed(currentFragment)
        d("Onboarding completed")
        appPreferences.onboardingCompleted = true
        val intent = Intent(this, MainActivity::class.java)
        startActivity(intent)
        finish()
    }

    override fun onNextPressed(currentFragment: Fragment?) {
        if (!appPreferences.isNotificationServiceAllowed) {
            Toast.makeText(this, "Allow notification access to submit listens", Toast.LENGTH_SHORT).show()
            val intent: Intent = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP_MR1) {
                Intent(Settings.ACTION_NOTIFICATION_LISTENER_SETTINGS)
            } else {
                Intent("android.settings.ACTION_NOTIFICATION_LISTENER_SETTINGS")
            }
            startActivity(intent)
        }
        else {
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
            if (
                featuresViewModel.appPreferences.onboardingCompleted &&
                featuresViewModel.appPreferences.isUserLoggedIn()
            ) {
                val intent = Intent(this@FeaturesActivity, MainActivity::class.java)
                startActivity(intent)
                finish()
            }
        }
        
    }
}