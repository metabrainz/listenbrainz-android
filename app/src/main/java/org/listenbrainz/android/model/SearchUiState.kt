package org.listenbrainz.android.model

data class SearchUiState<T>(
    val query: String,
    val result: T,
    val error: ResponseError?
)
