package org.listenbrainz.android.service

import org.listenbrainz.android.model.SocialResponse
import org.listenbrainz.android.model.feed.FeedData
import org.listenbrainz.android.model.feed.FeedEventDeletionData
import org.listenbrainz.android.model.feed.FeedEventVisibilityData

interface FeedServiceKtor {

    suspend fun getFeedEvents(
        username: String,
        count: Int = 25,
        maxTs: Int? = null,
        minTs: Int? = null
    ): FeedData

    suspend fun getFeedFollowListens(
        username: String,
        count: Int = 40,
        maxTs: Int? = null,
        minTs: Int? = null
    ): FeedData

    suspend fun getFeedSimilarListens(
        username: String,
        count: Int = 40,
        maxTs: Int? = null,
        minTs: Int? = null
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
