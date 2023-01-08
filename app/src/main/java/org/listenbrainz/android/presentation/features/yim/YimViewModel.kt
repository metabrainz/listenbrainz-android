package org.listenbrainz.android.presentation.features.yim

import android.content.Context
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import org.listenbrainz.android.data.repository.YimRepository
import org.listenbrainz.android.data.sources.api.entities.yimdata.*
import org.listenbrainz.android.presentation.features.login.LoginSharedPreferences
import org.listenbrainz.android.util.Resource
import org.listenbrainz.android.util.connectivityobserver.ConnectivityObserver
import org.listenbrainz.android.util.connectivityobserver.NetworkConnectivityObserver
import javax.inject.Inject

@HiltViewModel
class YimViewModel @Inject constructor(private val repository: YimRepository, @ApplicationContext context: Context) : ViewModel() {
    // Yim data resource
    var yimData:
            MutableState<
                    Resource<YimData>
                    >
    = mutableStateOf(Resource.loading())
    
    /** User name.
        Don't worry about this being nullable as we are performing login check.*/
    private var username: String? = LoginSharedPreferences.username
    
    // Network Checking variables
    private val connectivityObserver = NetworkConnectivityObserver(context)
    private var networkStatus:
            MutableState<ConnectivityObserver.NetworkStatus>
    = mutableStateOf(ConnectivityObserver.NetworkStatus.Unavailable)   // initial value
    
    init {
        checkNetworkStatus()
        getData()
    }
    
    private fun getData() {
        viewModelScope.launch {
            val response = async {  username?.let { repository.getYimData(username = it)}!! }
            when (response.await().status){
                Resource.Status.SUCCESS -> yimData.value = response.await()
                Resource.Status.LOADING -> yimData.value = Resource.loading()
                Resource.Status.FAILED -> yimData.value = Resource.failure()
            }
        }
    }
    
    // Username related functions
    fun getUserName() : String?{
        return username
    }
    fun isLoggedIn() : Boolean{
        return (LoginSharedPreferences.loginStatus == LoginSharedPreferences.STATUS_LOGGED_IN)
    }
    
    // Internet Connectivity Functions
    private fun checkNetworkStatus(){
        connectivityObserver.observe().onEach {
            networkStatus.value = it
        }.launchIn(viewModelScope)
    }
    fun getNetworkStatus() : ConnectivityObserver.NetworkStatus{
        return networkStatus.value
    }
    
    
    /** Get Data functions
     *  NOTE : Every get must be null checked.
     */
    
    fun getArtistMap() : ArrayList<ArtistMap>? {
        return yimData.value.data?.payload?.data?.artistMap
    }
    
    /** Get list of listen count of whole year.*/
    fun getListensListOfYear() : List<Int> {
         val list = arrayListOf<Int>()
         yimData.value.data?.payload?.data?.listensPerDay?.forEach {
             list.add(it.listenCount)
         }
        return list
    }
    
    /** List of new releases of those artists that the user listens to.*/
    fun getNewReleasesOfTopArtists() : ArrayList<NewReleasesOfTopArtist>? {
        return yimData.value.data?.payload?.data?.newReleasesOfTopArtists
    }
    
    /** The year of which the user listened most songs of. */
    fun getMostListenedYear() : Int? {
        val mapEntry = yimData.value.data?.payload?.data?.mostListenedYear?.maxBy {
            it.value
        }
        return mapEntry?.value
    }
    
    /** The day user listens the most music, every week.*/
    fun getDayOfWeek() : String? {
        return yimData.value.data?.payload?.data?.dayOfWeek
    }
    
    /** List of other ListenBrainz users with the same taste as user.
     *
     *  @return `null` for users with less listens.
     */
    fun getSimilarUsers(): List<Pair<String, Double>> {
        val list = yimData.value.data?.payload?.data?.similarUsers!!.toList()
        return list.sortedByDescending {
            it.second
        }
    }
    
    /** List of top artists of which user listened songs of*/
    fun getTopArtists() : ArrayList<TopArtist>? {
        return yimData.value.data?.payload?.data?.topArtists
    }
    
    /** Warning: Volatile fields (Might be null) :
     * @param caaId
     * @param caaReleaseMbid
     * @param releaseMbid
     */
    fun getTopRecordings() : ArrayList<TopRecording>? {
        return yimData.value.data?.payload?.data?.topRecordings
    }
    
    /** Top releases user listened to.*/
    fun getTopReleases() : ArrayList<TopRelease>? {
        return yimData.value.data?.payload?.data?.topReleases
    }
    
    /** Total of all artists the user listened to.*/
    fun getTotalArtistCount() : Int? {
        return yimData.value.data?.payload?.data?.totalArtistsCount
    }
    
    fun getTotalListenCount() : Int? {
        return yimData.value.data?.payload?.data?.totalListenCount
    }
    
    fun getTotalListeningTime() : Double? {
        return yimData.value.data?.payload?.data?.totalListeningTime
    }
    
    fun getTotalNewArtistsDiscovered() : Int? {
        return yimData.value.data?.payload?.data?.totalNewArtistsDiscovered
    }
    
    fun getTotalRecordingsCount() : Int? {
        return yimData.value.data?.payload?.data?.totalRecordingsCount
    }
    
    fun getTotalReleasesCount() : Int? {
        return yimData.value.data?.payload?.data?.totalReleasesCount
    }
    
    /** [getUrlsForAlbumArt]
     * @return Url list for Album Art. To be used with `R.drawable.yim_frame` */
    fun getUrlsForAlbumArt(isTopDiscoveriesPlaylist: Boolean) : List<String> {
        val map = if (isTopDiscoveriesPlaylist){
            yimData.value.data?.payload?.data?.topDiscoveriesPlaylistCoverArt
        }else{
            yimData.value.data?.payload?.data?.topMissedPlaylistCoverArt
        }
        val list = arrayListOf<String>()
        map?.onEachIndexed { index, entry ->
            if (index < 9){
                list.add(entry.value.replaceAfterLast(delimiter = '_', replacement = "thumb250.jpg"))   // This is done to smaller images.
            }
        }
        return list
    }
    
    /** [getTopDiscoveriesPlaylistAndArtCover]
     * @return map of [Track] (track) and [String] (Url of art cover) for [TopDiscoveriesPlaylist].*/
    fun getTopDiscoveriesPlaylistAndArtCover() : Map<Track, String>{
        val listOfTracks = yimData.value.data?.payload?.data?.topDiscoveriesPlaylist?.tracksList
        val artCoverMap = yimData.value.data?.payload?.data?.topDiscoveriesPlaylistCoverArt
        
        val resultMap = mutableMapOf<Track, String>()
    
        listOfTracks!!.forEach { track ->
            val mbid = track.identifier.substringAfterLast('/')
        
            if (artCoverMap!!.containsKey(mbid)){
                artCoverMap.forEach {
                    if (it.key == mbid){
                        resultMap[track] = it.value.replaceAfterLast(delimiter = '_', replacement = "thumb250.jpg")
                    }
                }
            }else{
                resultMap[track] = "null"
            }
        }
        
        return resultMap
    }
    
    /** [getTopMissedPlaylistAndArtCover]
     * @return map of [Track] (track) and [String] (Url of art cover) for [TopMissedPlaylist].*/
    fun getTopMissedPlaylistAndArtCover() : Map<Track, String>{
        val listOfTracks = yimData.value.data?.payload?.data?.topMissedPlaylist?.tracksList
        val artCoverMap = yimData.value.data?.payload?.data?.topMissedPlaylistCoverArt
    
        val resultMap = mutableMapOf<Track, String>()
        
        listOfTracks!!.forEach { track ->
            val mbid = track.identifier.substringAfterLast('/')
            
            if (artCoverMap!!.containsKey(mbid)){
                artCoverMap.forEach {
                    if (it.key == mbid){
                        resultMap[track] = it.value.replaceAfterLast(delimiter = '_', replacement = "thumb250.jpg")
                    }
                }
            }else{
                resultMap[track] = "null"
            }
        }
    
        return resultMap
    }
}