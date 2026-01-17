package org.listenbrainz.sharedtest.utils

import kotlin.test.assertEquals
import kotlin.test.assertNull
import org.listenbrainz.android.model.SocialData
import org.listenbrainz.android.model.yimdata.YimPayload
import org.listenbrainz.android.util.Resource
import org.listenbrainz.sharedtest.utils.EntityTestUtils.testUsername

object AssertionUtils {
    
    fun checkYimAssertions(testYimData: YimPayload, yimData : YimPayload?) {
        assertEquals(testYimData, yimData)
        assertEquals(testUsername, yimData?.payload?.userName)
    }
    
    fun checkFollowingAssertions(result: Resource<SocialData>, expected: SocialData) {
        assertEquals(expected.following, result.data?.following)
        assertEquals(expected.user, result.data?.user)
        assertNull(result.data?.followers)
        assertNull(result.error)
    }
    
    fun checkFollowersAssertions(result: Resource<SocialData>, expected: SocialData) {
        assertEquals(expected.followers, result.data?.followers)
        assertEquals(expected.user, result.data?.user)
        assertNull(result.data?.following)
        assertNull(result.error)
    }
}