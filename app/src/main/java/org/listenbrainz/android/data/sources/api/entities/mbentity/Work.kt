package org.listenbrainz.android.data.sources.api.entities.mbentity

internal class Work : MBEntity() {
    //TODO: Implement Relations
    var title: String? = null
    override fun toString(): String {
        return "Work{" +
                "title='" + title + '\'' +
                ", mbid='" + mbid + '\'' +
                ", disambiguation='" + disambiguation + '\'' +
                '}'
    }
}