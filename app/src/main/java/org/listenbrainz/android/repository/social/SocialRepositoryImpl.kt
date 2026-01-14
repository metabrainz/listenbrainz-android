package org.listenbrainz.android.repository.social

import org.listenbrainz.android.model.PinData
import org.listenbrainz.android.model.PinnedRecording
import org.listenbrainz.android.model.RecommendationData
import org.listenbrainz.android.model.ResponseError
import org.listenbrainz.android.model.Review
import org.listenbrainz.android.model.SearchResult
import org.listenbrainz.android.model.SimilarUserData
import org.listenbrainz.android.model.SocialData
import org.listenbrainz.android.model.SocialResponse
import org.listenbrainz.android.model.feed.FeedEvent
import org.listenbrainz.android.service.SocialService
import org.listenbrainz.android.util.Resource
import org.listenbrainz.shared.repository.AppPreferences
import org.listenbrainz.android.util.Utils.parseResponse



class SocialRepositoryImpl(
    private val service: SocialService,
    private val appPreferences: AppPreferences
) : SocialRepository {

    /** @return Network Failure, User DNE, Success.*/
    override suspend fun getFollowers(username: String?) : Resource<SocialData> = parseResponse {
        failIf(username == null) { ResponseError.AuthHeaderNotFound() }
        service.getFollowersData(username = username!!)
    }
    
    /** @return Network Failure, User DNE, Success.*/
    override suspend fun getFollowing(username: String) : Resource<SocialData> = parseResponse {
        service.getFollowingData(username = username)
    }
    
    /** @return Network Failure, User DNE, User already followed, Success.*/
    override suspend fun followUser(username: String): Resource<SocialResponse> = parseResponse {
        service.followUser(username = username)
    }
    
    /** Apparently server does not return 400 in case a user is not followed already.
     * @return Network Failure, User DNE, Success.
     */
    override suspend fun unfollowUser(username: String): Resource<SocialResponse> = parseResponse {
        service.unfollowUser(username = username)
    }
    
    /** @return Network Failure, User DNE, Success. */
    override suspend fun getSimilarUsers(username: String): Resource<SimilarUserData> = parseResponse {
        service.getSimilarUsersData(username = username)
    }
    
    /** @return Network Failure, [RATE_LIMIT_EXCEEDED], Success. */
    override suspend fun searchUser(username: String): Resource<SearchResult> = parseResponse {
        service.searchUser(username = username)
    }
    
    override suspend fun postPersonalRecommendation(username: String?, data: RecommendationData): Resource<FeedEvent> = parseResponse {
        failIf(username.isNullOrEmpty()) { ResponseError.AuthHeaderNotFound() }
        failIf(data.metadata.recordingMbid == null && data.metadata.recordingMsid == null) {
            ResponseError.BadRequest(actualResponse = "Cannot recommend this track.")
        }
        
        service.postPersonalRecommendation(
            username = username!!,
            data = data
        )
    }
    
    override suspend fun postRecommendationToAll(username: String?, data: RecommendationData): Resource<FeedEvent> = parseResponse {
        failIf(username.isNullOrEmpty()) { ResponseError.AuthHeaderNotFound() }
        failIf(data.metadata.recordingMbid == null && data.metadata.recordingMsid == null) {
            ResponseError.BadRequest(actualResponse = "Cannot recommend this track.")
        }
        
        service.postRecommendationToAll(
            username = username!!,
            data = data
        )
    }
    
    override suspend fun postReview(username: String?, data: Review): Resource<FeedEvent> = parseResponse {
        failIf(username.isNullOrEmpty()) { ResponseError.AuthHeaderNotFound() }
        failIf(data.metadata?.text.orEmpty().length < 25) {
            ResponseError.BadRequest(actualResponse = "Review is too short. Please write a review longer than 25 letters.")
        }
        failIf(data.metadata?.rating != null && data.metadata.rating !in 1..5) { ResponseError.BadRequest() }
        
        service.postReview(
            username = username!!,
            data = data
        )
    }
    
    override suspend fun pin(
        recordingMsid: String?,
        recordingMbid: String?,
        blurbContent: String?,
        pinnedUntil: Int
    ): Resource<PinData> = parseResponse {
        failIf(recordingMsid == null && recordingMbid == null) {
            ResponseError.BadRequest(actualResponse = "Cannot pin this particular recording.")
        }
        
        service.postPin(
            data = PinnedRecording(
                recordingMsid = recordingMsid,
                recordingMbid = recordingMbid,
                blurbContent = blurbContent,
                pinnedUntil = pinnedUntil
            )
        )
    }
    
    override suspend fun deletePin(id: Int): Resource<SocialResponse> = parseResponse {
        service.deletePin(id)
    }
}