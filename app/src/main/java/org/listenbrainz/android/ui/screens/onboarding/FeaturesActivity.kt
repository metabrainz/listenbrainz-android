package org.listenbrainz.android.ui.screens.onboarding

import android.Manifest
import android.content.Intent
import android.os.Build
import android.os.Bundle
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import com.limurse.onboard.OnboardAdvanced
import com.limurse.onboard.OnboardFragment
import com.limurse.onboard.OnboardPageTransformerType
import dagger.hilt.android.AndroidEntryPoint
import org.listenbrainz.android.R
import org.listenbrainz.android.repository.preferences.AppPreferences
import org.listenbrainz.android.ui.screens.dashboard.DashboardActivity
import org.listenbrainz.android.util.Log.d
import javax.inject.Inject

@AndroidEntryPoint
class FeaturesActivity : OnboardAdvanced() {
    @Inject
    lateinit var appPreferences: AppPreferences

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

//        setSignInButton(true)
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
                titleColor = ContextCompat.getColor(applicationContext, R.color.white),
                descriptionColor = ContextCompat.getColor(applicationContext, R.color.white),
                isLottie = true
            )
        )

        addSlide(
            OnboardFragment.newInstance(
                "Critiques",
                "Read and write about an album or event",
                resourceId = R.raw.review,
                backgroundColor =  ContextCompat.getColor(applicationContext, R.color.app_bg),
                titleColor = ContextCompat.getColor(applicationContext, R.color.white),
                descriptionColor = ContextCompat.getColor(applicationContext, R.color.white),
                isLottie = true
            )
        )

        addSlide(
            OnboardFragment.newInstance(
                "BrainzPlayer",
                "Listen to locally saved music",
                resourceId = R.raw.music_player,
                backgroundColor =  ContextCompat.getColor(applicationContext, R.color.app_bg),
                titleColor = ContextCompat.getColor(applicationContext, R.color.white),
                descriptionColor = ContextCompat.getColor(applicationContext, R.color.white),
                isLottie = true
            )
        )

        setTransformer(OnboardPageTransformerType.Parallax())
    }

    override fun onDonePressed(currentFragment: Fragment?) {
        super.onDonePressed(currentFragment)
        d("Onboarding completed")
        appPreferences.onboardingCompleted = true
        val intent = Intent(this, DashboardActivity::class.java)
        startActivity(intent)
        finish()
    }
}