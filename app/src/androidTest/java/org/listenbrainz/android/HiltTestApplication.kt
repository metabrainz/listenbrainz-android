package org.listenbrainz.android

import android.app.Application
import android.content.Context
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
}