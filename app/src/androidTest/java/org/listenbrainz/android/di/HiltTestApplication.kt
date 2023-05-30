package org.listenbrainz.android.di

import android.app.Application
import android.content.Context
import android.os.Bundle
import android.provider.Settings.Global.ANIMATOR_DURATION_SCALE
import android.provider.Settings.Global.TRANSITION_ANIMATION_SCALE
import android.provider.Settings.Global.WINDOW_ANIMATION_SCALE
import androidx.test.platform.app.InstrumentationRegistry
import androidx.test.runner.AndroidJUnitRunner
import dagger.hilt.android.testing.HiltTestApplication


/**
 * [CustomTestRunner]: A custom runner to set up the instrumented application class for tests.
 *
 * How to inject in a test?
 * 1) Annotate with @HiltAndroidTest
 * 2) Boilerplate code (inside the test):
 *      ```
 *      @get:Rule(order = 0)
 *      val hiltRule = HiltAndroidRule(this)
 *      ```
 * 3) Declare
 *      ```
 *      @Inject
 *      lateinit var injectable : Injectable
 *      ```
 * 4) Inject
 *      ```
 *      @Before
 *      fun init(){
 *          hiltrule.inject()
 *      }
 *      ```
 * @throws error DO NOT INJECT A DEPENDENCY WHICH ITSELF HAS INJECTIONS.
 */
class CustomTestRunner : AndroidJUnitRunner() {
    override fun newApplication(cl: ClassLoader?, name: String?, context: Context?): Application {
        return super.newApplication(cl, HiltTestApplication::class.java.name, context)
    }
    
    override fun onCreate(arguments: Bundle?) {
        super.onCreate(arguments)
        setAnimations(false)
    }
    
    override fun finish(resultCode: Int, results: Bundle?) {
        setAnimations(true)
        super.finish(resultCode, results)
    }
    
    private fun setAnimations(enabled: Boolean) {
        val value = if (enabled) "1.0" else "0.0"
        InstrumentationRegistry.getInstrumentation().uiAutomation.run {
            this.executeShellCommand("settings put global $WINDOW_ANIMATION_SCALE $value")
            this.executeShellCommand("settings put global $TRANSITION_ANIMATION_SCALE $value")
            this.executeShellCommand("settings put global $ANIMATOR_DURATION_SCALE $value")
        }
    }
}