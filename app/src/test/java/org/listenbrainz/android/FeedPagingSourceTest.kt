package org.listenbrainz.android

import androidx.paging.PagingSource
import kotlinx.coroutines.ExperimentalCoroutinesApi
import org.junit.Test
import org.junit.runner.RunWith
import org.listenbrainz.android.model.feed.FeedData
import org.listenbrainz.android.ui.screens.feed.FollowListensPagingSource
import org.listenbrainz.sharedtest.utils.ResourceString
import org.listenbrainz.sharedtest.utils.ResourceString.toClass
import org.mockito.junit.MockitoJUnitRunner

@OptIn(ExperimentalCoroutinesApi::class)
@RunWith(MockitoJUnitRunner::class)
class FeedPagingSourceTest: BaseUnitTest() {
    @Test
    fun test() {
        val expectedFeedData =
            FollowListensPagingSource.processFeedEvents(
                ResourceString.my_feed_page_1.toClass<FeedData>()
            )
        val expectedResult = PagingSource.LoadResult.Page(
            data = expectedFeedData,
            prevKey = null,
            nextKey = expectedFeedData.last().event.created
        )
    }
}