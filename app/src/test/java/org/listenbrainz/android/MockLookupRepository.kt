package org.listenbrainz.android

import com.google.gson.Gson
import org.listenbrainz.android.EntityTestUtils.loadResourceAsString
import org.listenbrainz.android.data.repository.LookupRepository
import org.listenbrainz.android.data.sources.api.entities.CoverArt
import org.listenbrainz.android.data.sources.api.entities.RecordingItem
import org.listenbrainz.android.data.sources.api.entities.WikiSummary
import org.listenbrainz.android.data.sources.api.entities.mbentity.Recording
import org.listenbrainz.android.data.sources.api.entities.mbentity.Release
import org.listenbrainz.android.util.Resource
import org.listenbrainz.android.util.Resource.Status.SUCCESS

class MockLookupRepository : LookupRepository {

    override suspend fun fetchData(entity: String, MBID: String, params: String?): Resource<String> {
        return Resource(SUCCESS, loadResourceAsString(entity + "_lookup.json"))
    }

    override suspend fun fetchWikiSummary(string: String, method: Int): Resource<WikiSummary> {
        val response = loadResourceAsString("artist_wiki.json")
        val summary = Gson().fromJson(response, WikiSummary::class.java)
        return Resource(SUCCESS, summary)
    }

    override suspend fun fetchCoverArt(MBID: String?): Resource<CoverArt> {
        val response = loadResourceAsString("cover_art.json")
        val coverArt = Gson().fromJson(response, CoverArt::class.java)
        return Resource(SUCCESS, coverArt)
    }

    override suspend fun fetchRecordings(artist: String?, title: String?): Resource<List<RecordingItem>> {
        TODO("Not yet implemented")
    }

    override suspend fun fetchMatchedRelease(MBID: String?): Resource<Release> {
        TODO("Not yet implemented")
    }

    override suspend fun fetchAcoustIDResults(duration: Long, fingerprint: String?): Resource<List<Recording>> {
        TODO("Not yet implemented")
    }
}