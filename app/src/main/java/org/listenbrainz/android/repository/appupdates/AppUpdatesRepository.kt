package org.listenbrainz.android.repository.appupdates

import android.content.Context
import org.listenbrainz.android.model.InstallSource
import org.listenbrainz.android.model.githubupdates.GithubUpdatesList
import org.listenbrainz.android.util.Resource

interface AppUpdatesRepository {

    suspend fun getAppReleasesFromGithub(): Resource<GithubUpdatesList>

    fun detectInstallSource(context: Context): InstallSource
}
