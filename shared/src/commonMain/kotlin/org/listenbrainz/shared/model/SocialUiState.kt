package org.listenbrainz.shared.model

import kotlinx.serialization.Serializable
import kotlinx.serialization.Transient
import org.jetbrains.compose.resources.StringResource

@Serializable
data class SocialUiState(
    val searchResult: List<String> = emptyList(),
    @Transient val error: ResponseError? = null,
    @Transient val successMsgId: StringResource? = null
)