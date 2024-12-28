package org.listenbrainz.android.model.feed

import androidx.compose.runtime.Immutable

@Immutable
data class FeedCallbacks(
    val onScrollToTop: (suspend () -> Unit) -> Unit,
    val onDeleteOrHide: (event: FeedEvent, eventType: FeedEventType, parentUser: String) -> Unit,
    val onErrorShown: () -> Unit,
    val onRecommend: (event: FeedEvent) -> Unit,
    val onPersonallyRecommend: (event: FeedEvent, users: List<String>, blurbContent: String) -> Unit,
    val onReview: (event: FeedEvent, entityType: ReviewEntityType, blurbContent: String, rating: Int?, locale: String) -> Unit,
    val onPin: (event: FeedEvent, blurbContent: String?) -> Unit,
    val searchFollower: (String) -> Unit,
    val isCritiqueBrainzLinked: suspend () -> Boolean?,
    val onPlay: (event: FeedEvent) -> Unit,
    val goToUserPage: (String) -> Unit,
    val goToArtistPage: (String) -> Unit
)
