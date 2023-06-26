package org.listenbrainz.android.model

import org.listenbrainz.android.util.ResponseError

data class SearchUiState(val query: String, val result: List<User>, val error: ResponseError?)
