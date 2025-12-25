package org.listenbrainz.android.repository.listens

import android.graphics.drawable.Drawable
import org.listenbrainz.android.model.CoverArt
import org.listenbrainz.android.model.Listen
import org.listenbrainz.android.model.ListenBrainzExternalServices
import org.listenbrainz.android.model.ListenSubmitBody
import org.listenbrainz.android.model.Listens
import org.listenbrainz.android.model.PostResponse
import org.listenbrainz.android.model.TokenValidation
import org.listenbrainz.android.util.Resource

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
    
    fun getPackageIcon(packageName: String): Drawable?
    
    fun getPackageLabel(packageName: String): String
    
    suspend fun submitListen(token: String, body: ListenSubmitBody): Resource<PostResponse>
    
    suspend fun getLinkedServices(token: String?, username: String?) : Resource<ListenBrainzExternalServices>

    suspend fun getNowPlaying(username: String?) : Resource<Listens>
    suspend fun deleteListen(listen: Listen): Resource<Void>
}