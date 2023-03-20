package org.listenbrainz.android

import android.app.Instrumentation
import android.content.Intent
import android.net.Uri
import androidx.test.espresso.Espresso
import androidx.test.espresso.action.ViewActions
import androidx.test.espresso.intent.Intents
import androidx.test.espresso.intent.matcher.IntentMatchers
import androidx.test.espresso.intent.rule.IntentsRule
import androidx.test.espresso.matcher.ViewMatchers
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
    //var loginTestRule = ActivityScenarioRule(ComponentActivity::class.java)
    var loginTestRule = ActivityScenarioRule(LoginActivity::class.java)
    
    var code = "Nlaa7v15QHm9g8rUOmT3dQ"

    @Before
    fun stubInternetIntent() {
        Intents.intending(IntentMatchers.hasAction(Intent.ACTION_VIEW))
            .respondWith(Instrumentation.ActivityResult(
                0,
                Intent().setData(Uri.parse(ListenBrainzServiceGenerator.OAUTH_REDIRECT_URI + "?code=" + code))
            ))
    }

    @Test
    fun testLoginAuthorization() {
        Espresso.onView(ViewMatchers.withId(R.id.login_btn)).perform(ViewActions.click())
        /*loginTestRule.scenario.onActivity {
            val intent = Intent(
                Intent.ACTION_VIEW,
                Uri.parse(
                    ListenBrainzServiceGenerator.AUTH_BASE_URL
                            + "authorize"
                            + "?response_type=code"
                            + "&client_id=" + ListenBrainzServiceGenerator.CLIENT_ID
                            + "&redirect_uri=" + ListenBrainzServiceGenerator.OAUTH_REDIRECT_URI
                            + "&scope=profile%20collection%20tag%20rating"
                )
            )
            it.startActivity(intent)
        }*/
        
        Intents.intended(AllOf.allOf(IntentMatchers.hasAction(Intent.ACTION_VIEW),
            IntentMatchers.hasData(Uri.parse(
                ListenBrainzServiceGenerator.AUTH_BASE_URL
                        + "authorize"
                        + "?response_type=code"
                        + "&client_id=" + ListenBrainzServiceGenerator.CLIENT_ID
                        + "&redirect_uri=" + ListenBrainzServiceGenerator.OAUTH_REDIRECT_URI
                        + "&scope=profile%20collection%20tag%20rating"))))
    }
}