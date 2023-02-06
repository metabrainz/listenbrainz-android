package org.listenbrainz.sharedtest.utils

import org.junit.Assert.assertEquals
import org.listenbrainz.android.data.sources.api.entities.yimdata.YimData
import org.listenbrainz.sharedtest.utils.EntityTestUtils.testYimUsername

object AssertionUtils {
    fun checkYimAssertions(testYimData: YimData?, yimData : YimData) {
        assertEquals(testYimData, yimData)
        assertEquals(testYimUsername, yimData.payload.userName)
    }
}