package org.listenbrainz.android.repository.social

import org.listenbrainz.android.model.EventsResponse
import org.listenbrainz.android.model.PinData
import org.listenbrainz.android.model.PinnedRecording
import org.listenbrainz.android.model.RecommendationData
import org.listenbrainz.android.model.ResponseError
import org.listenbrainz.android.model.ResponseError.Companion.getError
import org.listenbrainz.android.model.Review
import org.listenbrainz.android.model.SearchResult
import org.listenbrainz.android.model.SimilarUserData
import org.listenbrainz.android.model.SocialData
import org.listenbrainz.android.model.SocialResponse
import org.listenbrainz.android.model.feed.FeedEvent
import org.listenbrainz.android.service.SocialService
import org.listenbrainz.android.util.Resource
import org.listenbrainz.android.util.Utils.logAndReturn
import javax.inject.Inject


class SocialRepositoryImpl @Inject constructor(
    private val service: SocialService
) : SocialRepository {

    /** @return Network Failure, User DNE, Success.*/
    override suspend fun getFollowers(username: String?) : Resource<SocialData> =
        runCatching {
            if (username == null) return@runCatching Resource.failure(error = ResponseError.AUTH_HEADER_NOT_FOUND)
            
            val response = service.getFollowersData(username = username)
    
            return@runCatching if (response.isSuccessful) {
                Resource.success(response.body()!!)
            } else {
                Resource.failure(error = getError(response = response))
            }
    
        }.getOrElse { logAndReturn(it) }
    
    
    /** @return Network Failure, User DNE, Success.*/
    override suspend fun getFollowing(username: String) : Resource<SocialData> =
        runCatching {
            val response = service.getFollowingData(username = username)
    
            return@runCatching if (response.isSuccessful) {
                Resource.success(response.body()!!)
            } else {
                Resource.failure(error = getError(response = response))
            }
    
        }.getOrElse { logAndReturn(it) }
    
    
    /** @return Network Failure, User DNE, User already followed, Success.*/
    override suspend fun followUser(username: String): Resource<SocialResponse> =
        runCatching {
            val response = service.followUser(username = username)
    
            return@runCatching if (response.isSuccessful) {
                Resource.success(response.body()!!)
            } else {
                Resource.failure(error = getError(response = response))
            }
    
        }.getOrElse { logAndReturn(it) }
    
    
    /** Apparently server does not return 400 in case a user is not followed already.
     * @return Network Failure, User DNE, Success.
     */
    override suspend fun unfollowUser(username: String): Resource<SocialResponse> =
        runCatching {
            val response = service.unfollowUser(username = username)
            return@runCatching if (response.isSuccessful) {
                Resource.success(response.body()!!)
            } else {
                Resource.failure(error = getError(response = response))
            }
            
        }.getOrElse { logAndReturn(it) }
    
    
    /** @return Network Failure, User DNE, Success. */
    override suspend fun getSimilarUsers(username: String): Resource<SimilarUserData> =
        runCatching {
            val response = service.getSimilarUsersData(username = username)
            return@runCatching if (response.isSuccessful) {
                Resource.success(response.body()!!)
            } else {
                Resource.failure(error = getError(response = response))
            }
    
        }.getOrElse { logAndReturn(it) }
    
    
    /** @return Network Failure, [RATE_LIMIT_EXCEEDED], Success. */
    override suspend fun searchUser(username: String): Resource<SearchResult> =
        runCatching {
            val response = service.searchUser(username = username)
            return@runCatching if (response.isSuccessful) {
                Resource.success(response.body()!!)
            } else {
                Resource.failure(error = getError(response = response))
            }
    
        }.getOrElse { logAndReturn(it) }
    
    override suspend fun postPersonalRecommendation(username: String?, data: RecommendationData): Resource<FeedEvent> =
        runCatching {
            if (username.isNullOrEmpty())
                return@runCatching Resource.failure(error = ResponseError.AUTH_HEADER_NOT_FOUND)
            if (data.metadata.recordingMbid == null && data.metadata.recordingMsid == null)
                return@runCatching Resource.failure(error = ResponseError.BAD_REQUEST.apply { actualResponse = "Cannot recommend this track." })
            
            val response = service.postPersonalRecommendation(
                username = username,
                data = data
            )
            return@runCatching if (response.isSuccessful) {
                Resource.success(response.body()!!)
            } else {
                Resource.failure(error = getError(response = response))
            }
    
        }.getOrElse { logAndReturn(it) }
    
    override suspend fun postRecommendationToAll(username: String?, data: RecommendationData): Resource<FeedEvent> =
        runCatching {
            if (username.isNullOrEmpty())
                return@runCatching Resource.failure(error = ResponseError.AUTH_HEADER_NOT_FOUND)
            if (data.metadata.recordingMbid == null && data.metadata.recordingMsid == null)
                return@runCatching Resource.failure(error = ResponseError.BAD_REQUEST.apply { actualResponse = "Cannot recommend this track." })
            
            val response = service.postRecommendationToAll(
                username = username,
                data = data
            )
            
            return@runCatching if (response.isSuccessful) {
                Resource.success(response.body()!!)
            } else {
                Resource.failure(error = getError(response = response))
            }
    
        }.getOrElse { logAndReturn(it) }
    
    override suspend fun postReview(username: String?, data: Review): Resource<FeedEvent> =
        runCatching {
            if (username.isNullOrEmpty()) return@runCatching Resource.failure(error = ResponseError.AUTH_HEADER_NOT_FOUND)
            if (data.metadata.text.length < 25) return@runCatching Resource.failure(error = ResponseError.BAD_REQUEST.apply { actualResponse = "Review is too short. Please write a review longer than 25 letters." })
            if (data.metadata.rating != null && data.metadata.rating !in 1..5) return@runCatching  Resource.failure(error = ResponseError.BAD_REQUEST)
            
            val response = service.postReview(
                username = username,
                data = data
            )
            
            return@runCatching if (response.isSuccessful) {
                Resource.success(response.body()!!)
            } else {
                Resource.failure(error = getError(response = response))
            }
    
        }.getOrElse { logAndReturn(it) }
    
    override suspend fun pin(
        recordingMsid: String?,
        recordingMbid: String?,
        blurbContent: String?,
        pinnedUntil: Int
    ): Resource<PinData> =
        runCatching {
        
            if (recordingMsid == null && recordingMbid == null)
                return@runCatching Resource.failure(error = ResponseError.BAD_REQUEST.apply { actualResponse = "Cannot pin this particular recording." })
            
            val response = service.postPin(
                data = PinnedRecording(
                    recordingMsid = recordingMsid,
                    recordingMbid = recordingMbid,
                    blurbContent = blurbContent,
                    pinnedUntil = pinnedUntil
                )
            )
        
            return@runCatching if (response.isSuccessful) {
                Resource.success(response.body()!!)
            } else {
                Resource.failure(error = getError(response = response))
            }
        
        }.getOrElse { logAndReturn(it) }
    
    
    override suspend fun deletePin(id: Int): Resource<SocialResponse> =
        runCatching {
        
            val response = service.deletePin(id)
        
            return@runCatching if (response.isSuccessful) {
                Resource.success(response.body()!!)
            } else {
                Resource.failure(error = getError(response = response))
            }
        
        }.getOrElse { logAndReturn(it) }

    override suspend fun fetchEvents(artistId: String): Resource<EventsResponse> =
        runCatching {

            val response = service.getEvents(artistId, "json")

            return@runCatching if (response.isSuccessful) {
                Resource.success(response.body()!!)
            } else {
                Resource.failure(error = getError(response = response))
            }

        }.getOrElse { logAndReturn(it) }
}