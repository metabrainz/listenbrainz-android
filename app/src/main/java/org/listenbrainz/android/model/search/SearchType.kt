package org.listenbrainz.android.model.search

enum class SearchType(
    val title: String,
    val placeholder: String,
    val resultMessage: String
) {
    USER("User", "Search User", "No such users found"),
    PLAYLIST("Playlist", "Search Playlist", "No such playlists found"),
    ARTIST("Artist", "Search Artist", "No such artists found"),
    ALBUM("Album", "Search Album", "No such albums found"),
    TRACK("Track", "Search Track", "No such tracks found"),
}