package org.listenbrainz.android.repository.user

import org.listenbrainz.shared.model.user.AllPinnedRecordings
import org.listenbrainz.shared.model.CurrentPins
import org.listenbrainz.shared.model.Listens
import org.listenbrainz.shared.model.userPlaylist.UserPlaylistPayload
import org.listenbrainz.shared.model.user.TopAlbums
import org.listenbrainz.shared.model.user.TopArtists
import org.listenbrainz.shared.model.user.TopSongs
import org.listenbrainz.shared.model.user.UserFeedback
import org.listenbrainz.shared.model.user.UserListeningActivity
import org.listenbrainz.shared.model.user.UserSimilarityPayload
import org.listenbrainz.shared.util.Resource

interface UserRepository {
    suspend fun fetchUserListenCount (username: String?) : Resource<Listens?>
    suspend fun fetchUserSimilarity(username: String? , otherUserName: String?) : Resource<UserSimilarityPayload?>
    suspend fun fetchUserCurrentPins(username: String?) : Resource<CurrentPins?>
    suspend fun fetchUserPins(username: String?) : Resource<AllPinnedRecordings?>
    //TODO: Move to artists VM once implemented
    suspend fun getTopArtists(username: String?, rangeString: String = "all_time", count: Int = 25): Resource<TopArtists>
    suspend fun getUserFeedback(username: String?, score: Int?): Resource<UserFeedback?>
    suspend fun getUserListeningActivity(username: String?, rangeString: String = "all_time"): Resource<UserListeningActivity?>
    suspend fun getGlobalListeningActivity(rangeString: String = "all_time"): Resource<UserListeningActivity?>
    suspend fun getTopAlbums(username: String?, rangeString: String = "all_time" ,count: Int = 25): Resource<TopAlbums>
    suspend fun getTopSongs(username: String?, rangeString: String = "all_time"): Resource<TopSongs>
    suspend fun getCreatedForYouPlaylists(username: String?): Resource<UserPlaylistPayload>
}