package org.listenbrainz.android.benchmark

import androidx.benchmark.macro.StartupMode
import androidx.benchmark.macro.StartupTimingMetric
import androidx.benchmark.macro.junit4.MacrobenchmarkRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

/**
 * This benchmark navigates to the device's home screen, and launches the default activity.
 *
 * Before running benchmarks:
 * 1) Switch your app's active build variant in the Studio (from build dropdown on top bar) (affects Studio runs only)
 * 2) Only run on real device otherwise test will fail with error on emulators.
 */
@RunWith(AndroidJUnit4::class)
class StartupBenchmark {
    @get:Rule
    val benchmarkRule = MacrobenchmarkRule()
    
    @Test
    fun startup() = benchmarkRule.measureRepeated(
        packageName = "org.listenbrainz.android",
        metrics = listOf(StartupTimingMetric()),
        iterations = 5,
        startupMode = StartupMode.COLD
    ) {
        pressHome()
        startActivityAndWait()
    }
}