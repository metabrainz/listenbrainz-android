package org.listenbrainz.android.repository.user

import org.listenbrainz.android.model.CurrentPins
import org.listenbrainz.android.model.Listens
import org.listenbrainz.android.model.ResponseError
import org.listenbrainz.android.model.createdForYou.UserPlaylistPayload
import org.listenbrainz.android.model.user.AllPinnedRecordings
import org.listenbrainz.android.model.user.TopAlbums
import org.listenbrainz.android.model.user.TopArtists
import org.listenbrainz.android.model.user.TopSongs
import org.listenbrainz.android.model.user.UserFeedback
import org.listenbrainz.android.model.user.UserListeningActivity
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


    override suspend fun fetchUserSimilarity(username: String?, otherUserName: String?) : Resource<UserSimilarityPayload?> = parseResponse {
        if(username.isNullOrEmpty() or otherUserName.isNullOrEmpty()) return ResponseError.BAD_REQUEST.asResource()
        service.getUserSimilarity(username,otherUserName)
    }

    override suspend fun fetchUserCurrentPins(username: String?): Resource<CurrentPins?> = parseResponse {
        if(username.isNullOrEmpty()) return ResponseError.BAD_REQUEST.asResource()
        service.getUserCurrentPins(username)
    }

    override suspend fun fetchUserPins(username: String?): Resource<AllPinnedRecordings?> = parseResponse{
        if(username.isNullOrEmpty()) return ResponseError.BAD_REQUEST.asResource()
        service.getUserPins(username)
    }

    override suspend fun getTopArtists(
        username: String?,
        rangeString: String,
        count: Int
    ): Resource<TopArtists> = parseResponse {
        if(username.isNullOrEmpty()) return ResponseError.BAD_REQUEST.asResource()
        service.getTopArtistsOfUser(username, rangeString, count)
    }

    override suspend fun getUserFeedback(username: String?, score: Int?): Resource<UserFeedback?> = parseResponse {
        if(username.isNullOrEmpty()) return ResponseError.BAD_REQUEST.asResource()
        service.getUserFeedback(username, score)
    }

    override suspend fun getUserListeningActivity(
        username: String?,
        rangeString: String
    ): Resource<UserListeningActivity?> = parseResponse {
        if(username.isNullOrEmpty()) return ResponseError.BAD_REQUEST.asResource()
        service.getUserListeningActivity(username,rangeString)
    }

    override suspend fun getGlobalListeningActivity(rangeString: String): Resource<UserListeningActivity?> = parseResponse {
        service.getGlobalListeningActivity(rangeString)
    }

    override suspend fun getTopAlbums(
        username: String?,
        rangeString: String,
        count: Int
    ): Resource<TopAlbums> = parseResponse {
        if(username.isNullOrEmpty()) return ResponseError.BAD_REQUEST.asResource()
        service.getTopAlbumsOfUser(username, rangeString)
    }

    override suspend fun getTopSongs(username: String?, rangeString: String): Resource<TopSongs>  = parseResponse{
        if(username.isNullOrEmpty()) return ResponseError.BAD_REQUEST.asResource()
        service.getTopSongsOfUser(username, rangeString)
    }

    override suspend fun getCreatedForYouPlaylists(username: String?): Resource<UserPlaylistPayload> {
        return parseResponse {
            if(username.isNullOrEmpty()) return ResponseError.BAD_REQUEST.asResource()
            service.getCreatedForYouPlaylists(username)
        }
    }

}