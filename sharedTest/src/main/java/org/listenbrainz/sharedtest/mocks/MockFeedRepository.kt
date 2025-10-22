package org.listenbrainz.sharedtest.mocks

import org.listenbrainz.android.model.SocialResponse
import org.listenbrainz.android.model.feed.FeedData
import org.listenbrainz.android.model.feed.FeedEventDeletionData
import org.listenbrainz.android.model.feed.FeedEventVisibilityData
import org.listenbrainz.android.repository.feed.FeedRepository
import org.listenbrainz.android.util.Resource

class MockFeedRepository : FeedRepository {
    override suspend fun getFeedEvents(
        username: String?,
        maxTs: Long?,
        minTs: Long?,
        count: Int
    ): Resource<FeedData> {
        TODO("Not yet implemented")
    }

    override suspend fun getFeedFollowListens(
        username: String?,
        maxTs: Long?,
        minTs: Long?,
        count: Int
    ): Resource<FeedData> {
        TODO("Not yet implemented")
    }

    override suspend fun getFeedSimilarListens(
        username: String?,
        maxTs: Long?,
        minTs: Long?,
        count: Int
    ): Resource<FeedData> {
        TODO("Not yet implemented")
    }

    override suspend fun deleteEvent(
        username: String?,
        data: FeedEventDeletionData
    ): Resource<SocialResponse> {
        TODO("Not yet implemented")
    }

    override suspend fun hideEvent(
        username: String?,
        data: FeedEventVisibilityData
    ): Resource<SocialResponse> {
        TODO("Not yet implemented")
    }

    override suspend fun unhideEvent(
        username: String?,
        data: FeedEventVisibilityData
    ): Resource<SocialResponse> {
        TODO("Not yet implemented")
    }

}