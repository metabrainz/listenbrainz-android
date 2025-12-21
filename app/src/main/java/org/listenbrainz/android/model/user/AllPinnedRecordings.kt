package org.listenbrainz.android.model.user

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import org.listenbrainz.android.model.PinnedRecording

@Serializable
data class AllPinnedRecordings(
    @SerialName("pinned_recordings") val pinnedRecordings: List<PinnedRecording>? = listOf(),
    @SerialName("total_count") val totalCount: Int? = 0,
    @SerialName("user_name") val userName: String? = "",
    val count: Int? = 0,
    val offset: Int? = 0,
)