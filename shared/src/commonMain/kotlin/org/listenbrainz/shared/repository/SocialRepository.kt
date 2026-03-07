package org.listenbrainz.shared.repository

import org.listenbrainz.shared.model.feed.FeedEvent
import org.listenbrainz.shared.social.RecommendationData
import org.listenbrainz.shared.social.Resource
import org.listenbrainz.shared.social.SocialData


interface SocialRepository{
    suspend fun getFollowers(username: String): Resource<SocialData>
    suspend fun postRecommendationToAll(username: String?, data: RecommendationData): Resource<FeedEvent>
    suspend fun postPersonalRecommendation(username: String?, data: RecommendationData): Resource<FeedEvent>


}