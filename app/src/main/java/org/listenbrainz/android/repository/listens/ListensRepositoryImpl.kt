package org.listenbrainz.android.repository.listens

import android.content.pm.PackageManager
import android.graphics.drawable.Drawable
import org.listenbrainz.android.application.App
import org.listenbrainz.android.model.CoverArt
import org.listenbrainz.android.model.ListenBrainzExternalServices
import org.listenbrainz.android.model.ListenSubmitBody
import org.listenbrainz.android.model.Listens
import org.listenbrainz.android.model.PostResponse
import org.listenbrainz.android.model.ResponseError
import org.listenbrainz.android.model.TokenValidation
import org.listenbrainz.android.service.ListensService
import org.listenbrainz.android.util.Resource
import org.listenbrainz.android.util.Utils.parseResponse
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ListensRepositoryImpl @Inject constructor(val service: ListensService) : ListensRepository {
    
    override suspend fun fetchUserListens(username: String?): Resource<Listens> = parseResponse {
        if (username.isNullOrEmpty())
            return ResponseError.AUTH_HEADER_NOT_FOUND.asResource()
        
        service.getUserListens(username = username, count = 100)
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
    override fun getPackageIcon(packageName: String): Drawable? {
        return try {
            App.context.packageManager.getApplicationIcon(packageName)
        }
        catch (e: Exception) {
            null
        }
    }

    override fun getPackageLabel(packageName: String): String {
        return try {
            val info = App.context.packageManager.getApplicationInfo(packageName, 0)
            App.context.packageManager.getApplicationLabel(info).toString()
        } catch (e: PackageManager.NameNotFoundException) {
            packageName
        }
    }
    
    
    override suspend fun submitListen(token: String, body: ListenSubmitBody): Resource<PostResponse> = parseResponse {
        service.submitListen(body)
    }
    
    override suspend fun getLinkedServices(token: String?, username: String?): Resource<ListenBrainzExternalServices> = parseResponse {
        if (token.isNullOrEmpty() || username.isNullOrEmpty())
            return ResponseError.AUTH_HEADER_NOT_FOUND.asResource()
        
        service.getServicesLinkedToAccount(username = username)
    }
    
}