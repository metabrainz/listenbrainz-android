package org.listenbrainz.android.utiltest

import okhttp3.ResponseBody.Companion.toResponseBody
import org.junit.Assert.assertEquals
import org.junit.Test
import org.listenbrainz.android.model.ApiError
import org.listenbrainz.android.model.SocialData
import org.listenbrainz.android.model.SocialResponse
import org.listenbrainz.android.util.ErrorUtil.getSocialErrorType
import org.listenbrainz.android.util.ErrorUtil.parseError
import org.listenbrainz.android.util.ResponseError
import org.listenbrainz.sharedtest.utils.EntityTestUtils.ErrorUtil.alreadyFollowingError
import org.listenbrainz.sharedtest.utils.EntityTestUtils.ErrorUtil.authHeaderNotFoundError
import org.listenbrainz.sharedtest.utils.EntityTestUtils.ErrorUtil.cannotFollowSelfError
import org.listenbrainz.sharedtest.utils.EntityTestUtils.ErrorUtil.userNotFoundError
import org.listenbrainz.sharedtest.utils.ResourceString.cannot_follow_self_error
import org.listenbrainz.sharedtest.utils.ResourceString.user_does_not_exist_error
import retrofit2.Response

class ResponseErrorUtilTests {
    
    @Test
    fun parseErrorTest() {
        var error = parseError(Response.error<SocialData>(404, user_does_not_exist_error.toResponseBody()))
        assertEquals(ApiError(404, userNotFoundError), error)
        
        error = parseError(Response.error<SocialResponse>(400, cannot_follow_self_error.toResponseBody()))
        assertEquals(ApiError(400, cannotFollowSelfError), error)
    }
    
    @Test
    fun getSocialErrorTypeTest() {
        var result = getSocialErrorType(userNotFoundError)
        assertEquals(ResponseError.USER_NOT_FOUND, result)
        
        result = getSocialErrorType(authHeaderNotFoundError)
        assertEquals(ResponseError.AUTH_HEADER_NOT_FOUND, result)
        
        result = getSocialErrorType(alreadyFollowingError)
        assertEquals(ResponseError.ALREADY_FOLLOWING, result)
        
        result = getSocialErrorType(cannotFollowSelfError)
        assertEquals(ResponseError.CANNOT_FOLLOW_SELF, result)
        
        result = getSocialErrorType("Wow new error")
        assertEquals(ResponseError.UNKNOWN, result)
    }
}