package org.listenbrainz.shared.repository.feed

import org.listenbrainz.shared.model.SocialResponse
import org.listenbrainz.shared.model.feed.FeedData
import org.listenbrainz.shared.model.feed.FeedEventDeletionData
import org.listenbrainz.shared.model.feed.FeedEventVisibilityData
import org.listenbrainz.shared.util.Resource

interface FeedRepository {

    suspend fun getFeedEvents(
        username: String?,
        maxTs: Long? = null,
        minTs: Long? = null,
        count: Int = FeedEventCount
    ): Resource<FeedData>

    suspend fun getFeedFollowListens(
        username: String?,
        maxTs: Long? = null,
        minTs: Long? = null,
        count: Int = FeedListensCount
    ): Resource<FeedData>

    suspend fun getFeedSimilarListens(
        username: String?,
        maxTs: Long? = null,
        minTs: Long? = null,
        count: Int = FeedListensCount
    ): Resource<FeedData>

    suspend fun deleteEvent(
        username: String?,
        data: FeedEventDeletionData
    ): Resource<SocialResponse>

    suspend fun hideEvent(
        username: String?,
        data: FeedEventVisibilityData
    ): Resource<SocialResponse>

    suspend fun unhideEvent(
        username: String?,
        data: FeedEventVisibilityData
    ): Resource<SocialResponse>
    
    companion object {
        const val FeedEventCount = 25
        const val FeedListensCount = 40
    }
}