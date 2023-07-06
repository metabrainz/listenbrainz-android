package org.listenbrainz.android.model

data class SearchUiState(
    val query: String,
    val result: UserListUiState,
    val error: ResponseError?
)
