package org.listenbrainz.android.model

import kotlinx.serialization.Serializable
import kotlinx.serialization.Transient
import org.listenbrainz.shared.model.ResponseError

@Serializable
data class SocialUiState(
    val searchResult: List<String> = emptyList(),
    @Transient val error: ResponseError? = null,
    val successMsgId: Int? = null
)