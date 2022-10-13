package org.listenbrainz.android.data.sources

import org.listenbrainz.android.data.sources.api.entities.mbentity.MBEntityType

object Constants {
    const val LOOKUP_ARTIST_PARAMS = "url-rels+releases+ratings+tags"
    const val LOOKUP_RELEASE_PARAMS = "recordings+url-rels+artist-credits"
    const val LOOKUP_LABEL_PARAMS = "releases+url-rels+ratings+tags"
    const val LOOKUP_RECORDING_PARAMS = "releases+media+url-rels+artists+artist-credits+ratings+tags"
    const val LOOKUP_RELEASE_GROUP_PARAMS = "releases+artist-credits+url-rels+release-rels+media+ratings+tags"
    const val TAGGER_RELEASE_PARAMS = "release-groups+media+recordings+" +
            "artist-credits+artists+aliases+labels+isrcs+collections+artist-rels+release-rels+" +
            "url-rels+recording-rels+work-rels"
    const val ACOUST_ID_RESPONSE_PARAMS = "recordings releasegroups releases " +
            "tracks compress sources"
    const val USER_DATA_PARAMS = "+user-tags+user-ratings"

    //Urls for adding in the database
    const val ADD_ARTIST = "https://musicbrainz.org/artist/create?edit-artist"
    const val ADD_RELEASE = "https://musicbrainz.org/release/add"
    const val ADD_EVENT = "https://musicbrainz.org/event/create?edit-event"
    const val ADD_RELEASEGROUP = "https://musicbrainz.org/release-group/create?edit-release-group"
    const val ADD_LABEL = "https://musicbrainz.org/label/create?edit-label"
    const val ADD_RECORDING = "https://musicbrainz.org/recording/create?edit-recording"
    const val LIMIT = 10
    const val OFFSET = 0
    const val MBID = "mbid"
    const val TYPE = "type"

    fun getDefaultParams(entity: MBEntityType?): String? {
        return when (entity) {
            MBEntityType.ARTIST -> LOOKUP_ARTIST_PARAMS
            MBEntityType.LABEL -> LOOKUP_LABEL_PARAMS
            MBEntityType.RELEASE -> LOOKUP_RELEASE_PARAMS
            MBEntityType.RECORDING -> LOOKUP_RECORDING_PARAMS
            MBEntityType.RELEASE_GROUP -> LOOKUP_RELEASE_GROUP_PARAMS
            else -> null
        }
    }
}