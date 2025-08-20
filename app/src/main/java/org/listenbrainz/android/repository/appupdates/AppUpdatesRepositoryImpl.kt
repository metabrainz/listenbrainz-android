package org.listenbrainz.android.repository.appupdates

import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.withContext
import org.listenbrainz.android.di.IoDispatcher
import org.listenbrainz.android.model.githubupdates.GithubUpdatesList
import org.listenbrainz.android.service.GithubAppUpdatesService
import org.listenbrainz.android.util.Resource
import org.listenbrainz.android.util.Utils.parseResponse
import javax.inject.Inject

class AppUpdatesRepositoryImpl @Inject constructor(
    private val githubAppUpdatesService: GithubAppUpdatesService,
    @IoDispatcher private val ioDispatcher: CoroutineDispatcher
) : AppUpdatesRepository {

    override suspend fun getAppReleasesFromGithub(): Resource<GithubUpdatesList> =
        withContext(ioDispatcher) {
            parseResponse {
                githubAppUpdatesService.getAppReleases()
            }
        }
}
