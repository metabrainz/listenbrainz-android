package org.listenbrainz.shared.repository.listens

import org.listenbrainz.shared.model.ListenBrainzExternalServices
import org.listenbrainz.shared.model.ListenSubmitBody
import org.listenbrainz.shared.model.Listens
import org.listenbrainz.shared.model.PostResponse
import org.listenbrainz.shared.model.TokenValidation
import org.listenbrainz.shared.util.Resource
import org.listenbrainz.shared.model.CoverArt

interface ListensRepository {

    suspend fun fetchUserListens(
        username: String?,
        count: Int = 100,
        maxTs: Long? = null,
        minTs: Long? = null
    ): Resource<Listens>

    suspend fun fetchCoverArt(mbid: String): Resource<CoverArt>

    /** Inputs token from Auth header.*/
    suspend fun validateToken(token: String): Resource<TokenValidation>

    fun getPackageIcon(packageName: String): Any?

    fun getPackageLabel(packageName: String): String
    suspend fun deleteListen(
        listenedAt: Long,
        recordingMsid: String
    ): Resource<PostResponse>

    suspend fun submitListen(token: String, body: ListenSubmitBody): Resource<PostResponse>

    suspend fun getLinkedServices(token: String?, username: String?): Resource<ListenBrainzExternalServices>

    suspend fun getNowPlaying(username: String?): Resource<Listens>
}