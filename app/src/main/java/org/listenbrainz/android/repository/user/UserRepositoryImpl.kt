package org.listenbrainz.android.repository.user

import org.listenbrainz.android.model.CurrentPins
import org.listenbrainz.android.model.Listens
import org.listenbrainz.android.model.ResponseError
import org.listenbrainz.android.model.user.AllPinnedRecordings
import org.listenbrainz.android.model.userPlaylist.UserPlaylistPayload
import org.listenbrainz.android.model.user.TopAlbums
import org.listenbrainz.android.model.user.TopArtists
import org.listenbrainz.android.model.user.TopSongs
import org.listenbrainz.android.model.user.UserFeedback
import org.listenbrainz.android.model.user.UserListeningActivity
import org.listenbrainz.android.model.user.UserSimilarityPayload
import org.listenbrainz.android.service.UserService
import org.listenbrainz.android.util.Resource
import org.listenbrainz.android.repository.preferences.AppPreferences
import org.listenbrainz.android.util.Utils.parseResponse


class UserRepositoryImpl(
    private val service: UserService,
    private val appPreferences: AppPreferences
) : UserRepository {
    override suspend fun fetchUserListenCount(username: String?): Resource<Listens?> = parseResponse {
        failIf(username.isNullOrEmpty()) { ResponseError.DoesNotExist() }
        service.getListenCount(username)
    }


    override suspend fun fetchUserSimilarity(username: String?, otherUserName: String?): Resource<UserSimilarityPayload?> = parseResponse {
        failIf(username.isNullOrEmpty() || otherUserName.isNullOrEmpty()) { ResponseError.BadRequest() }
        service.getUserSimilarity(username, otherUserName)
    }

    override suspend fun fetchUserCurrentPins(username: String?): Resource<CurrentPins?> = parseResponse {
        failIf(username.isNullOrEmpty()) { ResponseError.BadRequest() }
        service.getUserCurrentPins(username)
    }

    override suspend fun fetchUserPins(username: String?): Resource<AllPinnedRecordings?> = parseResponse {
        failIf(username.isNullOrEmpty()) { ResponseError.BadRequest() }
        service.getUserPins(username)
    }

    override suspend fun getTopArtists(
        username: String?,
        rangeString: String,
        count: Int
    ): Resource<TopArtists> = parseResponse {
        failIf(username.isNullOrEmpty()) { ResponseError.BadRequest() }
        service.getTopArtistsOfUser(username, rangeString, count)
    }

    override suspend fun getUserFeedback(username: String?, score: Int?): Resource<UserFeedback?> = parseResponse {
        failIf(username.isNullOrEmpty()) { ResponseError.BadRequest() }
        service.getUserFeedback(username, score)
    }

    override suspend fun getUserListeningActivity(
        username: String?,
        rangeString: String
    ): Resource<UserListeningActivity?> = parseResponse {
        failIf(username.isNullOrEmpty()) { ResponseError.BadRequest() }
        service.getUserListeningActivity(username, rangeString)
    }

    override suspend fun getGlobalListeningActivity(rangeString: String): Resource<UserListeningActivity?> = parseResponse {
        service.getGlobalListeningActivity(rangeString)
    }

    override suspend fun getTopAlbums(
        username: String?,
        rangeString: String,
        count: Int
    ): Resource<TopAlbums> = parseResponse {
        failIf(username.isNullOrEmpty()) { ResponseError.BadRequest() }
        service.getTopAlbumsOfUser(username, rangeString)
    }

    override suspend fun getTopSongs(username: String?, rangeString: String): Resource<TopSongs> = parseResponse {
        failIf(username.isNullOrEmpty()) { ResponseError.BadRequest() }
        service.getTopSongsOfUser(username, rangeString)
    }

    override suspend fun getCreatedForYouPlaylists(username: String?): Resource<UserPlaylistPayload> {
        return parseResponse {
            failIf(username.isNullOrEmpty()) { ResponseError.BadRequest() }
            service.getCreatedForYouPlaylists(username)
        }
    }
}