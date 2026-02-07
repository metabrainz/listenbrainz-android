package org.listenbrainz.android.service

import de.jensklingenberg.ktorfit.http.GET
import org.listenbrainz.android.model.githubupdates.GithubUpdatesList

interface GithubAppUpdatesService {

    @GET("releases")
    suspend fun getAppReleases(): GithubUpdatesList
}
