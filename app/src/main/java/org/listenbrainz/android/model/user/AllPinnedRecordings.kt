package org.listenbrainz.android.model.user

import com.google.gson.annotations.SerializedName
import org.listenbrainz.android.model.PinnedRecording

data class AllPinnedRecordings(
    @SerializedName("pinned_recordings") val pinnedRecordings: List<PinnedRecording>? = listOf(),
    @SerializedName("total_count")       val totalCount: Int? = 0,
    @SerializedName("user_name")         val userName: String? = "",
                                         val count: Int? = 0,
                                         val offset: Int? = 0,
)