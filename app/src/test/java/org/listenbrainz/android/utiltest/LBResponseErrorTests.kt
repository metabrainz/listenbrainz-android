package org.listenbrainz.android.utiltest

import org.junit.Assert.assertEquals
import org.junit.Test
import org.listenbrainz.android.util.LBResponseError
import org.listenbrainz.android.util.ErrorUtil.getSocialErrorType
import org.listenbrainz.sharedtest.utils.EntityTestUtils.ErrorUtil.alreadyFollowingError
import org.listenbrainz.sharedtest.utils.EntityTestUtils.ErrorUtil.authHeaderNotFoundError
import org.listenbrainz.sharedtest.utils.EntityTestUtils.ErrorUtil.cannotFollowSelfError
import org.listenbrainz.sharedtest.utils.EntityTestUtils.ErrorUtil.userNotFoundError

class LBResponseErrorTests {
    
    @Test
    fun getSocialErrorTypeTest() {
        var result = getSocialErrorType(userNotFoundError)
        assertEquals(LBResponseError.UserNotFound, result)
        
        result = getSocialErrorType(authHeaderNotFoundError)
        assertEquals(LBResponseError.AuthHeaderNotFound, result)
        
        result = getSocialErrorType(alreadyFollowingError)
        assertEquals(LBResponseError.AlreadyFollowing, result)
        
        result = getSocialErrorType(cannotFollowSelfError)
        assertEquals(LBResponseError.CannotFollowSelf, result)
        
        result = getSocialErrorType("Wow new error")
        assertEquals(LBResponseError.Unknown, result)
    }
}