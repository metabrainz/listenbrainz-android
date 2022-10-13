package org.listenbrainz.android.data.sources.api

import okhttp3.ResponseBody
import org.listenbrainz.android.data.sources.api.entities.CoverArt
import org.listenbrainz.android.data.sources.api.entities.RecordingItem
import org.listenbrainz.android.data.sources.api.entities.WikiSummary
import org.listenbrainz.android.data.sources.api.entities.acoustid.AcoustIDResponse
import org.listenbrainz.android.data.sources.api.entities.mbentity.Release
import org.listenbrainz.android.data.sources.api.entities.response.BarcodeReleaseResponse
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path
import retrofit2.http.Query

interface LookupService {
    @GET("{entity}/{MBID}")
    suspend fun lookupEntityData(@Path("entity") entity: String,
                                 @Path("MBID") MBID: String,
                                 @Query("inc") params: String): ResponseBody

    @GET("http://coverartarchive.org/release/{MBID}")
    suspend fun getCoverArt(@Path("MBID") MBID: String?): CoverArt


    @GET("https://labs.api.listenbrainz.org/mbid-mapping/json")
    suspend fun searchRecording(@Query("[artist_credit_name]") artistCreditName: String?,
                        @Query("[recording_name]") recordingName: String?): ArrayList<RecordingItem>

    @GET("release/{MBID}")
    suspend fun lookupRecording(@Path("MBID") MBID: String?,
                        @Query("inc") params: String?): Release

    @POST("https://api.acoustid.org/v2/lookup")
    suspend fun lookupFingerprint(@Query("client") client: String?,
                          @Query("meta") metadata: String?,
                          @Query("duration") duration: Long,
                          @Query("fingerprint") fingerprint: String?): AcoustIDResponse

    @GET("https://en.wikipedia.org/api/rest_v1/page/summary/{title}")
    suspend fun getWikipediaSummary(@Path("title") title: String): WikiSummary

    @GET("https://www.wikidata.org/w/api.php" + "?action=wbgetentities&format=xml&props=sitelinks/urls&format=json")
    suspend fun getWikipediaLink(@Query("ids") id: String): ResponseBody

    @GET("release/")
    suspend fun lookupReleaseWithBarcode(@Query("query") barcode: String): BarcodeReleaseResponse
}