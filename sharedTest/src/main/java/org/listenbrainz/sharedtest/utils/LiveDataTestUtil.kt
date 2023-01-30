/* Copyright 2019 Google LLC.
   SPDX-License-Identifier: Apache-2.0 */
package org.listenbrainz.sharedtest.utils

import kotlin.Throws
import androidx.lifecycle.LiveData
import androidx.lifecycle.Observer
import java.lang.RuntimeException
import java.util.concurrent.CountDownLatch
import java.util.concurrent.TimeUnit

object LiveDataTestUtil {
    @Throws(InterruptedException::class)
    fun <T> getOrAwaitValue(liveData: LiveData<T>): T? {
        val data = arrayOfNulls<Any>(1)
        val latch = CountDownLatch(1)
        val observer = object : Observer<T?> {
            override fun onChanged(o: T?) {
                data[0] = o
                latch.countDown()
                liveData.removeObserver(this)
            }
        }
        liveData.observeForever(observer)
        if (!latch.await(2, TimeUnit.SECONDS)) {
            throw RuntimeException("LiveData value was never set.")
        }
        return data[0] as T?
    }
}