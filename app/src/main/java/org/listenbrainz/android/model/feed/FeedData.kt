package org.listenbrainz.android.model.feed

import androidx.compose.runtime.Immutable

@Immutable
data class FeedData(
    val payload: FeedPayload
)