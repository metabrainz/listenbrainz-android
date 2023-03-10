package org.listenbrainz.android.ui.screens.onboarding

import android.content.Intent
import android.os.Build
import android.os.Bundle
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import com.limurse.onboard.OnboardAdvanced
import com.limurse.onboard.OnboardFragment
import com.limurse.onboard.OnboardPageTransformerType
import org.listenbrainz.android.R
import org.listenbrainz.android.ui.screens.dashboard.DashboardActivity
import org.listenbrainz.android.util.UserPreferences

class FeaturesActivity : OnboardAdvanced() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

//        setSignInButton(true)
        isWizardMode = true

        when {
            Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU -> {
                askForPermissions(
                    permissions = arrayOf(android.Manifest.permission.POST_NOTIFICATIONS),
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

    public override fun onDonePressed(currentFragment: Fragment?) {
        super.onDonePressed(currentFragment)
        UserPreferences.setOnBoardingCompleted()
        val intent = Intent(this, DashboardActivity::class.java)
        startActivity(intent)
        finish()
    }
}