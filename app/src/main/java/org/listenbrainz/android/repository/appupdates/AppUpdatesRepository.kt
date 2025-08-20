package org.listenbrainz.android.repository.appupdates

import org.listenbrainz.android.model.githubupdates.GithubUpdatesList
import org.listenbrainz.android.util.Resource

interface AppUpdatesRepository {

    suspend fun getAppReleasesFromGithub(): Resource<GithubUpdatesList>
}
