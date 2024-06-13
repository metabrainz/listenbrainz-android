package org.listenbrainz.android.repository.user

import org.listenbrainz.android.model.Listens
import org.listenbrainz.android.model.PinnedRecording
import org.listenbrainz.android.model.ResponseError
import org.listenbrainz.android.model.user.UserSimilarity
import org.listenbrainz.android.model.user.UserSimilarityPayload
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

    override suspend fun fetchUserSimilarity(username: String?, otherUserName: String?) : Resource<UserSimilarityPayload?> = parseResponse {
        if(username.isNullOrEmpty() or otherUserName.isNullOrEmpty()) return ResponseError.BAD_REQUEST.asResource()
        service.getUserSimilarity(username,otherUserName)
    }

    override suspend fun fetchUserCurrentPins(username: String?): Resource<PinnedRecording?> = parseResponse {
        if(username.isNullOrEmpty()) return ResponseError.BAD_REQUEST.asResource()
        service.getUserCurrentPins(username)
    }



}