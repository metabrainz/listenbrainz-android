package org.listenbrainz.android.service

import de.jensklingenberg.ktorfit.http.GET
import org.listenbrainz.shared.model.githubupdates.GithubUpdatesList

interface GithubAppUpdatesService {

    @GET("releases")
    suspend fun getAppReleases(): GithubUpdatesList
}
