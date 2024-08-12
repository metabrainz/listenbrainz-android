package org.listenbrainz.sharedtest.mocks

import android.graphics.drawable.Drawable
import org.listenbrainz.android.model.CoverArt
import org.listenbrainz.android.model.ListenBrainzExternalServices
import org.listenbrainz.android.model.ListenSubmitBody
import org.listenbrainz.android.model.Listens
import org.listenbrainz.android.model.PostResponse
import org.listenbrainz.android.model.ResponseError
import org.listenbrainz.android.model.TokenValidation
import org.listenbrainz.android.repository.listens.ListensRepository
import org.listenbrainz.android.util.Resource
import org.listenbrainz.sharedtest.testdata.ListensRepositoryTestData.listensTestData

class MockListensRepository : ListensRepository {
    override suspend fun fetchUserListens(username: String?): Resource<Listens> {
        return if(username.isNullOrEmpty()){
            ResponseError.DOES_NOT_EXIST.asResource()
        } else{
            Resource(Resource.Status.SUCCESS, listensTestData)
        }
    }

    override suspend fun fetchCoverArt(mbid: String): Resource<CoverArt> {
        TODO("Not yet implemented")
    }

    override suspend fun validateToken(token: String): Resource<TokenValidation> {
        TODO("Not yet implemented")
    }

    override fun getPackageIcon(packageName: String): Drawable? {
        TODO("Not yet implemented")
    }

    override fun getPackageLabel(packageName: String): String {
        TODO("Not yet implemented")
    }

    override suspend fun submitListen(
        token: String,
        body: ListenSubmitBody
    ): Resource<PostResponse> {
        TODO("Not yet implemented")
    }

    override suspend fun getLinkedServices(
        token: String?,
        username: String?
    ): Resource<ListenBrainzExternalServices> {
        TODO("Not yet implemented")
    }

}