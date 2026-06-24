package org.listenbrainz.sharedtest.mocks

import org.listenbrainz.shared.model.SocialResponse
import org.listenbrainz.shared.model.feed.FeedData
import org.listenbrainz.shared.model.feed.FeedEventDeletionData
import org.listenbrainz.shared.model.feed.FeedEventVisibilityData
import org.listenbrainz.shared.repository.feed.FeedRepository
import org.listenbrainz.shared.util.Resource

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