package org.listenbrainz.android.utiltest

import okhttp3.ResponseBody.Companion.toResponseBody
import org.junit.Assert.assertEquals
import org.junit.Test
import org.listenbrainz.android.model.ApiError
import org.listenbrainz.android.model.ResponseError
import org.listenbrainz.android.model.ResponseError.Companion.getError
import org.listenbrainz.android.model.ResponseError.Companion.parseError
import org.listenbrainz.android.model.SocialData
import org.listenbrainz.android.model.SocialResponse
import org.listenbrainz.sharedtest.testdata.SocialRepositoryTestData.ErrorUtil.cannotFollowSelfError
import org.listenbrainz.sharedtest.testdata.SocialRepositoryTestData.ErrorUtil.userNotFoundError
import org.listenbrainz.sharedtest.utils.ResourceString.already_following_error
import org.listenbrainz.sharedtest.utils.ResourceString.auth_header_not_found_error
import org.listenbrainz.sharedtest.utils.ResourceString.cannot_follow_self_error
import org.listenbrainz.sharedtest.utils.ResourceString.rate_limiting_error
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
    fun getErrorTest() {
        var result = getError(
            Response.error<SocialData>(
                404,
                user_does_not_exist_error.toResponseBody())
        )
        assertEquals(ResponseError.DOES_NOT_EXIST, result)
        
        result = getError(
            Response.error<SocialData>(
                401,
                auth_header_not_found_error.toResponseBody())
        )
        assertEquals(ResponseError.AUTH_HEADER_NOT_FOUND, result)
        
        result = getError(
            Response.error<SocialData>(
                400,
                already_following_error.toResponseBody())
        )
        assertEquals(ResponseError.BAD_REQUEST, result)
        
        result = getError(
            Response.error<SocialData>(
                400,
                cannot_follow_self_error.toResponseBody())
        )
        assertEquals(ResponseError.BAD_REQUEST, result)
        
        result = getError(
            Response.error<SocialData>(
                429,
                rate_limiting_error.toResponseBody())
        )
        assertEquals(ResponseError.RATE_LIMIT_EXCEEDED, result)
    }
}