package org.listenbrainz.android.repository.explore

import org.listenbrainz.android.model.explore.HueSoundPayload
import org.listenbrainz.android.util.Resource

interface ExploreRepository {

    suspend fun getReleasesFromColor(color: String): Resource<HueSoundPayload>
}