package org.listenbrainz.android.data.sources.api

import okhttp3.ResponseBody
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query

interface CollectionService {
    @GET("collection")
    suspend fun getPublicUserCollections(@Query("editor") name: String): ResponseBody

    @GET("collection")
    suspend fun getAllUserCollections(@Query("editor") name: String,
                                      @Query("inc") params: String): ResponseBody

    @GET("{entity}")
    suspend fun getCollectionContents(@Path("entity") entity: String,
                                      @Query("collection") id: String): ResponseBody
}