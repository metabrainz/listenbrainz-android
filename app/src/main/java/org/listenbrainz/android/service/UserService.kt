package org.listenbrainz.android.service

import org.listenbrainz.android.model.CurrentPins
import org.listenbrainz.android.model.Listens
import org.listenbrainz.android.model.userPlaylist.UserPlaylistPayload
import org.listenbrainz.android.model.user.AllPinnedRecordings
import org.listenbrainz.android.model.user.TopAlbums
import org.listenbrainz.android.model.user.TopArtists
import org.listenbrainz.android.model.user.TopSongs
import org.listenbrainz.android.model.user.UserFeedback
import org.listenbrainz.android.model.user.UserListeningActivity
import org.listenbrainz.android.model.user.UserSimilarityPayload
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query

interface UserService {
    @GET("user/{user_name}/listen-count")
    suspend fun getListenCount(@Path("user_name") username : String?) : Response<Listens?>

    @GET("user/{user_name}/similar-to/{other_user_name}")
    suspend fun getUserSimilarity(@Path("user_name") username: String? , @Path("other_user_name") otherUserName: String?) : Response<UserSimilarityPayload?>

    @GET("{user_name}/pins/current")
    suspend fun getUserCurrentPins(@Path("user_name") username: String?) : Response<CurrentPins?>

    @GET("{user_name}/pins")
    suspend fun getUserPins(@Path("user_name") username: String?) : Response<AllPinnedRecordings?>

    @GET("stats/user/{user_name}/artists")
    suspend fun getTopArtistsOfUser(@Path("user_name") username: String?, @Query("range") rangeString: String?, @Query("count") count: Int = 25) : Response<TopArtists>

    @GET("feedback/user/{user_name}/get-feedback?metadata=true")
    suspend fun getUserFeedback(@Path("user_name") username: String?, @Query("score") score: Int?) : Response<UserFeedback?>

    @GET("stats/user/{user_name}/listening-activity")
    suspend fun getUserListeningActivity(@Path("user_name") username: String?, @Query("range") rangeString: String?): Response<UserListeningActivity?>

    @GET("stats/sitewide/listening-activity")
    suspend fun getGlobalListeningActivity(@Query("range") rangeString: String?) : Response<UserListeningActivity?>

    @GET("stats/user/{user_name}/releases")
    suspend fun getTopAlbumsOfUser(@Path("user_name") username: String?, @Query("range") rangeString: String?): Response<TopAlbums>

    @GET("stats/user/{user_name}/recordings")
    suspend fun getTopSongsOfUser(@Path("user_name") username: String?, @Query("range") rangeString: String?): Response<TopSongs>

    @GET("user/{user_name}/playlists/createdfor")
    suspend fun getCreatedForYouPlaylists(@Path("user_name") username: String?): Response<UserPlaylistPayload>

    @GET("user/{user_name}/playlists")
    suspend fun getUserPlaylists(@Path("user_name") username: String?, @Query("offset") offset: Int, @Query("count") count: Int): Response<UserPlaylistPayload>

    @GET("user/{user_name}/playlists/collaborator")
    suspend fun getUserCollabPlaylists(@Path("user_name") username: String?, @Query("offset") offset: Int, @Query("count") count: Int): Response<UserPlaylistPayload>
}