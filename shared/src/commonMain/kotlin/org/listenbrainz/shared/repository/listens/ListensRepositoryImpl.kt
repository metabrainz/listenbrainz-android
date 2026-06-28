package org.listenbrainz.shared.repository.listens

import kotlinx.coroutines.CoroutineDispatcher
import org.listenbrainz.shared.model.DeleteListen
import org.listenbrainz.shared.model.ListenBrainzExternalServices
import org.listenbrainz.shared.model.ListenSubmitBody
import org.listenbrainz.shared.model.Listens
import org.listenbrainz.shared.model.PostResponse
import org.listenbrainz.shared.model.ResponseError
import org.listenbrainz.shared.model.TokenValidation
import org.listenbrainz.shared.model.dao.PendingListensDao
import org.listenbrainz.shared.repository.AppPreferences
import org.listenbrainz.shared.service.ListensService
import org.listenbrainz.shared.service.UserService
import org.listenbrainz.shared.util.Resource
import org.listenbrainz.shared.util.Utils.parseResponse
import org.listenbrainz.shared.model.CoverArt

abstract class ListensRepositoryImpl(
    private val service: ListensService,
    private val appPreferences: AppPreferences,
    private val userService: UserService,
    private val pendingListensDao: PendingListensDao,
    private val ioDispatcher: CoroutineDispatcher
) : ListensRepository {

    override suspend fun fetchUserListens(
        username: String?,
        count: Int,
        maxTs: Long?,
        minTs: Long?
    ): Resource<Listens> = parseResponse {
        failIf(username.isNullOrEmpty()) { ResponseError.AuthHeaderNotFound() }

        service.getUserListens(
            username = username,
            count = count,
            maxTs = maxTs,
            minTs = minTs
        )
    }

    override suspend fun fetchCoverArt(mbid: String): Resource<CoverArt> = parseResponse {
        service.getCoverArt(mbid)
    }
    
    override suspend fun validateToken(token: String): Resource<TokenValidation> = parseResponse {
        service.checkTokenValidity(authHeader = "Token $token")
    }

    /** Retrieve any installed application's icon. If the requested application is not installed, null is returned.
     *
     * Usage:
     * ```
     * Image(
     *    painter = rememberDrawablePainter( drawable = getPackageIcon(packageName) ),
     *    contentDescription = "Example Description"
     * )
     * ```
     * */
    override fun getPackageIcon(packageName: String): Any? = null

    override fun getPackageLabel(packageName: String): String = packageName

    override suspend fun deleteListen(
        listenedAt: Long,
        recordingMsid: String
    ): Resource<PostResponse> = parseResponse {
        service.deleteListen(
            payload = DeleteListen(
                listenedAt = listenedAt,
                recordingMsid = recordingMsid
            )
        )
    }


    override suspend fun submitListen(token: String, body: ListenSubmitBody): Resource<PostResponse> = parseResponse {
        service.submitListen(body)
    }
    
    override suspend fun getLinkedServices(token: String?, username: String?): Resource<ListenBrainzExternalServices> = parseResponse {
        failIf(token.isNullOrEmpty() || username.isNullOrEmpty()) { ResponseError.AuthHeaderNotFound() }
        
        service.getServicesLinkedToAccount(username = username)
    }

    override suspend fun getNowPlaying(username: String?): Resource<Listens> = parseResponse {
        failIf(username.isNullOrEmpty()) { ResponseError.AuthHeaderNotFound() }

        service.getNowPlaying(username = username)
    }
}
