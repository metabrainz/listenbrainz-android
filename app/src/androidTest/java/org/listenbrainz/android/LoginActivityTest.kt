package org.listenbrainz.android

import android.app.Instrumentation
import android.content.Intent
import android.net.Uri
import androidx.activity.ComponentActivity
import androidx.test.espresso.intent.Intents
import androidx.test.espresso.intent.matcher.IntentMatchers
import androidx.test.espresso.intent.rule.IntentsRule
import androidx.test.ext.junit.rules.ActivityScenarioRule
import androidx.test.filters.LargeTest
import androidx.test.internal.runner.junit4.AndroidJUnit4ClassRunner
import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
import org.hamcrest.core.AllOf
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.listenbrainz.android.ui.screens.login.LoginActivity
import org.listenbrainz.android.util.ListenBrainzServiceGenerator

@RunWith(AndroidJUnit4ClassRunner::class)
@LargeTest
@HiltAndroidTest
class LoginActivityTest {
    
    @get:Rule(order = 0)
    var hiltRule = HiltAndroidRule(this)
    
    @get:Rule(order = 1)
    var intentsTestRule = IntentsRule()
    
    @get:Rule(order = 2)
    var loginTestRule = ActivityScenarioRule(ComponentActivity::class.java)
    
    var code = "Nlaa7v15QHm9g8rUOmT3dQ"

    @Before
    fun setup() {
        
        // stubInternetIntent
        Intents.intending(IntentMatchers.hasAction(Intent.ACTION_VIEW))
            .respondWith(Instrumentation.ActivityResult(
                0,
                Intent().setData(Uri.parse(ListenBrainzServiceGenerator.OAUTH_REDIRECT_URI + "?code=" + code))
            ))
        
        // Starting login activity.
        loginTestRule.scenario.onActivity {
            it.startActivity(Intent(it, LoginActivity::class.java))
        }
    }

    @Test
    fun testLoginAuthorization() {
        Intents.intended(
            AllOf.allOf(IntentMatchers.hasAction(Intent.ACTION_VIEW),
                IntentMatchers.hasData(
                    Uri.parse(
                    ListenBrainzServiceGenerator.AUTH_BASE_URL
                            + "authorize"
                            + "?response_type=code"
                            + "&client_id=" + ListenBrainzServiceGenerator.CLIENT_ID
                            + "&redirect_uri=" + ListenBrainzServiceGenerator.OAUTH_REDIRECT_URI
                            + "&scope=profile%20collection%20tag%20rating"
                    )
                )
            )
        )
    }
}