package org.listenbrainz.shared.service

import org.listenbrainz.shared.model.SocialResponse
import org.listenbrainz.shared.model.feed.FeedData
import org.listenbrainz.shared.model.feed.FeedEventDeletionData
import org.listenbrainz.shared.model.feed.FeedEventVisibilityData

interface FeedServiceKtor {

    suspend fun getFeedEvents(
        username: String,
        count: Int = 25,
        maxTs: Long? = null,
        minTs: Long? = null
    ): FeedData

    suspend fun getFeedFollowListens(
        username: String,
        count: Int = 40,
        maxTs: Long? = null,
        minTs: Long? = null
    ): FeedData

    suspend fun getFeedSimilarListens(
        username: String,
        count: Int = 40,
        maxTs: Long? = null,
        minTs: Long? = null
    ): FeedData

    suspend fun deleteEvent(
        username: String,
        body: FeedEventDeletionData
    ): SocialResponse

    suspend fun hideEvent(
        username: String,
        body: FeedEventVisibilityData
    ): SocialResponse

    suspend fun unhideEvent(
        username: String,
        body: FeedEventVisibilityData
    ): SocialResponse
}
