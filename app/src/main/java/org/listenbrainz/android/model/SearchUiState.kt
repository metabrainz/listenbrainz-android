package org.listenbrainz.android.model

data class SearchUiState(val query: String, val result: List<User>, val error: ResponseError?)
