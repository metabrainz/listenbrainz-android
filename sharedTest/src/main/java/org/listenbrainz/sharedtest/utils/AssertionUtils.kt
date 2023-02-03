package org.listenbrainz.sharedtest.utils

import org.junit.Assert.assertEquals
import org.listenbrainz.android.data.sources.api.entities.yimdata.YimData

object AssertionUtils {
    fun checkYimAssertions(testYimData: YimData?, yimData : YimData) {
        assertEquals(testYimData, yimData)
    }
}