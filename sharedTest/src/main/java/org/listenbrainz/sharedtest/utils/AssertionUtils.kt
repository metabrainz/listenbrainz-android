package org.listenbrainz.sharedtest.utils

import org.junit.Assert.assertEquals
import org.listenbrainz.android.model.SocialData
import org.listenbrainz.android.model.yimdata.YimPayload
import org.listenbrainz.sharedtest.utils.EntityTestUtils.testUsername

object AssertionUtils {
    
    fun checkYimAssertions(testYimData: YimPayload?, yimData : YimPayload) {
        assertEquals(testYimData, yimData)
        assertEquals(testUsername, yimData.payload.userName)
    }
    
    fun checkFollowingAssertions(data: SocialData?, expected: SocialData) {
        assertEquals(expected.following, data?.following)
        assertEquals(expected.user, data?.user)
        assertEquals(null, data?.followers)
        assertEquals(null, data?.error)
    }
    
    fun checkFollowersAssertions(data: SocialData?, expected: SocialData) {
        assertEquals(expected.followers, data?.followers)
        assertEquals(expected.user, data?.user)
        assertEquals(null, data?.following)
        assertEquals(null, data?.error)
    }
}