package org.listenbrainz.shared.repository

import org.listenbrainz.shared.model.PinData
import org.listenbrainz.shared.model.Review
import org.listenbrainz.shared.model.feed.FeedEvent
import org.listenbrainz.shared.social.RecommendationData
import org.listenbrainz.shared.social.Resource
import org.listenbrainz.shared.social.SocialData
import org.listenbrainz.shared.social.SocialResponse
import kotlin.time.Clock


interface SocialRepository{
    suspend fun getFollowers(username: String): Resource<SocialData>
    suspend fun followUser(username: String) : Resource<SocialResponse>
    suspend fun unfollowUser(username: String) : Resource<SocialResponse>
    suspend fun postRecommendationToAll(username: String?, data: RecommendationData): Resource<FeedEvent>
    suspend fun postPersonalRecommendation(username: String?, data: RecommendationData): Resource<FeedEvent>

    suspend fun postReview(username: String?, data: Review): Resource<FeedEvent>
    suspend fun pin(recordingMsid: String?, recordingMbid: String?, blurbContent: String?, pinnedUntil: Int = Clock.System.now().epochSeconds.toInt()): Resource<PinData>
}