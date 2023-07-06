package org.listenbrainz.sharedtest.mocks

import org.listenbrainz.android.model.Error
import org.listenbrainz.android.model.SearchResult
import org.listenbrainz.android.model.SimilarUserData
import org.listenbrainz.android.model.SocialData
import org.listenbrainz.android.model.SocialResponse
import org.listenbrainz.android.repository.SocialRepository
import org.listenbrainz.android.util.Resource
import org.listenbrainz.sharedtest.testdata.SocialRepositoryTestData.testFollowUnfollowSuccessResponse
import org.listenbrainz.sharedtest.testdata.SocialRepositoryTestData.testFollowersSuccessData
import org.listenbrainz.sharedtest.testdata.SocialRepositoryTestData.testFollowingSuccessData
import org.listenbrainz.sharedtest.testdata.SocialRepositoryTestData.testSearchResult
import org.listenbrainz.sharedtest.testdata.SocialRepositoryTestData.testSimilarUserSuccessData

class MockSocialRepository(): SocialRepository {
    
    override suspend fun getFollowers(username: String): Resource<SocialData> {
        return Resource(Resource.Status.SUCCESS,testFollowersSuccessData)
    }
    
    override suspend fun getFollowing(username: String): Resource<SocialData> {
        return Resource(Resource.Status.SUCCESS, testFollowingSuccessData)
    }
    
    /** Success for this request.*/
    override suspend fun followUser(
        username: String,
        accessToken: String
    ): Resource<SocialResponse> {
        return Resource.success(testFollowUnfollowSuccessResponse)
    }
    
    /** Failure for this request.*/
    override suspend fun unfollowUser(
        username: String,
        accessToken: String
    ): Resource<SocialResponse> {
        return Resource.failure(error = Error.BAD_REQUEST)
    }
    
    override suspend fun getSimilarUsers(username: String): Resource<SimilarUserData> {
        return Resource(Resource.Status.SUCCESS, testSimilarUserSuccessData)
    }
    
    override suspend fun searchUser(username: String): Resource<SearchResult> {
        return Resource(Resource.Status.SUCCESS, testSearchResult)
    }
}