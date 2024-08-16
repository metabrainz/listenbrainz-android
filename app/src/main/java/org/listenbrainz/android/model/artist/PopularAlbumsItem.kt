package org.listenbrainz.android.model.artist

import com.google.gson.annotations.SerializedName

data class PopularAlbumsItem(
    val artist: TopAlbumArtistInfo? = TopAlbumArtistInfo(),
    val release: Release? = Release(),
    @SerializedName("release_color") val releaseColor: ReleaseColor? = ReleaseColor(),
    @SerializedName("release_group") val releaseGroup: Release? = Release(),
    @SerializedName("release_group_mbid") val releaseGroupMbid: String? = "",
    val tag: Tag? = Tag(),
    @SerializedName("total_listen_count") val totalListenCount: Int? = 0,
    @SerializedName("total_user_count") val totalUserCount: Int? = 0
)