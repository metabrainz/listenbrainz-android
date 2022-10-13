package org.listenbrainz.android.data.sources.api.entities.response

import org.listenbrainz.android.data.sources.api.entities.mbentity.Release
import java.util.*

class BarcodeReleaseResponse {
    var created: String? = null
    var count = 0
    var offset = 0
    var releases: List<Release> = ArrayList()
}