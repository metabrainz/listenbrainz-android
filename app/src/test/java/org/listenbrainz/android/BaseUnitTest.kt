package org.listenbrainz.android

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import junit.framework.TestCase
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.TestDispatcher
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import org.junit.Rule
import org.junit.runner.RunWith
import org.listenbrainz.sharedtest.utils.CoroutineTestRule
import org.mockito.junit.MockitoJUnitRunner


@ExperimentalCoroutinesApi
@RunWith(MockitoJUnitRunner::class)
abstract class BaseUnitTest(testDispatcher: TestDispatcher = UnconfinedTestDispatcher()) {
    
    @Rule
    @JvmField
    val rule = InstantTaskExecutorRule()
    
    @Rule
    @JvmField
    val coroutinesTestRule = CoroutineTestRule(testDispatcher)
    
    protected fun testDispatcher() = coroutinesTestRule.testDispatcher
    
    protected fun test(block: suspend TestScope.() -> Unit) = runTest(testDispatcher()) { block() }
    
    protected infix fun <T> T?.shouldBe(expected: T?) = TestCase.assertEquals(expected, this)
}