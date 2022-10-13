package org.listenbrainz.android.data.sources.api

import okhttp3.ResponseBody
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query

interface SearchService {
    @GET("{entity}/")
    suspend fun searchEntity(@Path("entity") entity: String?,
                             @Query("query") searchTerm: String?,
                             @Query("limit") limit: Int,
                             @Query("offset") offset: Int): ResponseBody?
}