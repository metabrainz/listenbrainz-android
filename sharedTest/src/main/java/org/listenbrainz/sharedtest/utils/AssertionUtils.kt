package org.listenbrainz.sharedtest.utils

import org.junit.Assert.assertEquals
import org.listenbrainz.android.model.yimdata.YimPayload
import org.listenbrainz.sharedtest.utils.EntityTestUtils.testUsername

object AssertionUtils {
    fun checkYimAssertions(testYimData: YimPayload?, yimData : YimPayload) {
        assertEquals(testYimData, yimData)
        assertEquals(testUsername, yimData.payload.userName)
    }
    
    fun checkFollowingAssertions(){
    
    }
}