package org.listenbrainz.android.repository

import android.graphics.drawable.Drawable
import org.listenbrainz.android.model.CoverArt
import org.listenbrainz.android.model.Listen
import org.listenbrainz.android.model.TokenValidation
import org.listenbrainz.android.util.Resource

interface ListensRepository {
    
    suspend fun fetchUserListens(userName: String): Resource<List<Listen>>
    
    suspend fun fetchCoverArt(MBID: String): Resource<CoverArt>

    suspend fun validateUserToken(token: String): Resource<TokenValidation>
    
    fun getPackageIcon(packageName: String): Drawable?
    
    fun getPackageLabel(packageName: String): String
}