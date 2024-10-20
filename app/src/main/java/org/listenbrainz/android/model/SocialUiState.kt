package org.listenbrainz.android.model

data class SocialUiState(
    val searchResult: List<String> = emptyList(),
    val error: ResponseError? = null,
    val successMsgId : Int? = null
)