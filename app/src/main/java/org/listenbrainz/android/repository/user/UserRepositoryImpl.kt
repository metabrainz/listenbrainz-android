package org.listenbrainz.android.repository.user

import org.listenbrainz.android.model.Listens
import org.listenbrainz.android.model.ResponseError
import org.listenbrainz.android.service.UserService
import org.listenbrainz.android.util.Resource
import org.listenbrainz.android.util.Utils.parseResponse
import javax.inject.Inject

class UserRepositoryImpl @Inject constructor(
    private val service: UserService
) : UserRepository {
    override suspend fun fetchUserListenCount(username: String?): Resource<Listens?> = parseResponse {
        if(username.isNullOrEmpty()) return ResponseError.DOES_NOT_EXIST.asResource()
        service.getListenCount(username)
    }

    override suspend fun fetchListeningNow(username: String?): Resource<Listens?> {
        TODO("Not yet implemented")
    }


}