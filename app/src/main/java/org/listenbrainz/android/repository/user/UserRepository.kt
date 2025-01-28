package org.listenbrainz.android.repository.user

import org.listenbrainz.android.model.CurrentPins
import org.listenbrainz.android.model.Listens
import org.listenbrainz.android.model.PinnedRecording
import org.listenbrainz.android.model.createdForYou.CreatedForYouPayload
import org.listenbrainz.android.model.createdForYou.CreatedForYouPlaylist
import org.listenbrainz.android.model.createdForYou.CreatedForYouPlaylists
import org.listenbrainz.android.model.user.AllPinnedRecordings
import org.listenbrainz.android.model.user.TopAlbums
import org.listenbrainz.android.model.user.TopArtists
import org.listenbrainz.android.model.user.TopSongs
import org.listenbrainz.android.model.user.UserFeedback
import org.listenbrainz.android.model.user.UserListeningActivity
import org.listenbrainz.android.model.user.UserSimilarityPayload
import org.listenbrainz.android.util.Resource

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
    suspend fun getCreatedForYouPlaylists(username: String?): Resource<CreatedForYouPayload>
}