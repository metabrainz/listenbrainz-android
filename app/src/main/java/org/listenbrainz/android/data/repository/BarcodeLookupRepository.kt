package org.listenbrainz.android.data.repository

import org.listenbrainz.android.data.sources.api.LookupService
import org.listenbrainz.android.data.sources.api.entities.mbentity.Release
import org.listenbrainz.android.util.Resource
import org.listenbrainz.android.util.Resource.Status.FAILED
import org.listenbrainz.android.util.Resource.Status.SUCCESS

import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class BarcodeLookupRepository @Inject constructor(private val service: LookupService) {

    suspend fun lookupReleasesWithBarcode(barcode: String): Resource<List<Release>> {
        return try {
            val result = service.lookupReleaseWithBarcode(barcode)
            Resource(SUCCESS, result.releases)
        } catch (e: Exception) {
            e.printStackTrace()
            Resource(FAILED, null)
        }
    }

}