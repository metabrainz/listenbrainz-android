package org.listenbrainz.android.utiltest

import okhttp3.ResponseBody.Companion.toResponseBody
import org.junit.Assert.assertEquals
import org.junit.Test
import org.listenbrainz.android.model.ApiError
import org.listenbrainz.android.model.GeneralError
import org.listenbrainz.android.model.ResponseError.Companion.getSocialErrorType
import org.listenbrainz.android.model.ResponseError.Companion.parseError
import org.listenbrainz.android.model.SocialData
import org.listenbrainz.android.model.SocialError
import org.listenbrainz.android.model.SocialResponse
import org.listenbrainz.sharedtest.testdata.SocialRepositoryTestData.ErrorUtil.alreadyFollowingError
import org.listenbrainz.sharedtest.testdata.SocialRepositoryTestData.ErrorUtil.authHeaderNotFoundError
import org.listenbrainz.sharedtest.testdata.SocialRepositoryTestData.ErrorUtil.cannotFollowSelfError
import org.listenbrainz.sharedtest.testdata.SocialRepositoryTestData.ErrorUtil.userNotFoundError
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
        var result = getSocialErrorType(userNotFoundError, 404)
        assertEquals(SocialError.USER_NOT_FOUND, result)
        
        result = getSocialErrorType(authHeaderNotFoundError, 401)
        assertEquals(GeneralError.AUTH_HEADER_NOT_FOUND, result)
        
        result = getSocialErrorType(alreadyFollowingError, 400)
        assertEquals(SocialError.ALREADY_FOLLOWING, result)
        
        result = getSocialErrorType(cannotFollowSelfError, 400)
        assertEquals(SocialError.CANNOT_FOLLOW_SELF, result)
        
        result = getSocialErrorType("", 429)
        assertEquals(GeneralError.RATE_LIMIT_EXCEEDED, result)
        
        result = getSocialErrorType("Wow new error", 400)
        assertEquals(GeneralError.UNKNOWN, result)
    }
}