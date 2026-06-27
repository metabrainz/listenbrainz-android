package org.listenbrainz.sharedtest.mocks

import org.listenbrainz.shared.model.PinData
import org.listenbrainz.shared.model.RecommendationData
import org.listenbrainz.shared.model.ResponseError
import org.listenbrainz.shared.model.Review
import org.listenbrainz.shared.model.SearchResult
import org.listenbrainz.shared.model.SimilarUserData
import org.listenbrainz.shared.model.SocialData
import org.listenbrainz.shared.model.SocialResponse
import org.listenbrainz.shared.model.feed.FeedEvent
import org.listenbrainz.shared.repository.social.SocialRepository
import org.listenbrainz.shared.util.Resource
import org.listenbrainz.sharedtest.testdata.SocialRepositoryTestData.testFollowersSuccessData
import org.listenbrainz.sharedtest.testdata.SocialRepositoryTestData.testFollowingSuccessData
import org.listenbrainz.sharedtest.testdata.SocialRepositoryTestData.testSimilarUserSuccessData

class MockSocialRepository : SocialRepository {
    override suspend fun getFollowers(username: String?): Resource<SocialData> {
        return if(username.isNullOrEmpty()){
            ResponseError.DoesNotExist().asResource()
        }
        else{
            Resource(Resource.Status.SUCCESS, testFollowersSuccessData)
        }
    }

    override suspend fun getFollowing(username: String): Resource<SocialData> {
        return Resource(Resource.Status.SUCCESS, testFollowingSuccessData)
    }

    override suspend fun followUser(username: String): Resource<SocialResponse> {
        TODO("Not yet implemented")
    }

    override suspend fun unfollowUser(username: String): Resource<SocialResponse> {
        TODO("Not yet implemented")
    }

    override suspend fun getSimilarUsers(username: String): Resource<SimilarUserData> {
        return Resource(Resource.Status.SUCCESS, testSimilarUserSuccessData)
    }

    override suspend fun searchUser(username: String): Resource<SearchResult> {
        TODO("Not yet implemented")
    }

    override suspend fun postPersonalRecommendation(
        username: String?,
        data: RecommendationData
    ): Resource<FeedEvent> {
        TODO("Not yet implemented")
    }

    override suspend fun postRecommendationToAll(
        username: String?,
        data: RecommendationData
    ): Resource<FeedEvent> {
        TODO("Not yet implemented")
    }

    override suspend fun postReview(username: String?, data: Review): Resource<FeedEvent> {
        TODO("Not yet implemented")
    }

    override suspend fun pin(
        recordingMsid: String?,
        recordingMbid: String?,
        blurbContent: String?,
        pinnedUntil: Int
    ): Resource<PinData> {
        TODO("Not yet implemented")
    }

    override suspend fun deletePin(id: Int): Resource<SocialResponse> {
        TODO("Not yet implemented")
    }

}