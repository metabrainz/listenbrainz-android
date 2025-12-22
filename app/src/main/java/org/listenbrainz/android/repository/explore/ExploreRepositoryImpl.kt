package org.listenbrainz.android.repository.explore

import jakarta.inject.Inject
import org.listenbrainz.android.model.explore.HueSoundPayload
import org.listenbrainz.android.service.ExploreService
import org.listenbrainz.android.util.Resource
import org.listenbrainz.android.util.Utils.parseResponse


class ExploreRepositoryImpl @Inject constructor(
    private val exploreService: ExploreService
) : ExploreRepository {
    override suspend fun getReleasesFromColor(color: String): Resource<HueSoundPayload> =
        parseResponse { exploreService.getReleasesFromColor(color) }
}