package org.listenbrainz.android.service

import org.listenbrainz.android.model.githubupdates.GithubUpdatesList
import retrofit2.Response
import retrofit2.http.GET

interface GithubAppUpdatesService {

    @GET("releases")
    suspend fun getAppReleases(): Response<GithubUpdatesList>
}
