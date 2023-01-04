package org.listenbrainz.android.presentation.features.yim

import android.content.Context
import android.os.Build
import androidx.annotation.RequiresApi
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
import org.listenbrainz.android.util.connectivityobserver.ConnectivityObserver
import org.listenbrainz.android.util.connectivityobserver.NetworkConnectivityObserver
import org.listenbrainz.android.util.Resource
import javax.inject.Inject

@HiltViewModel
class YimViewModel @Inject constructor(private val repository: YimRepository, @ApplicationContext context: Context) : ViewModel() {
    // Yim data resource
    private var yimData:
            MutableState<
                    Resource<YimData>?
                    >
            = mutableStateOf(null)
    
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
    
    fun getData() {
        viewModelScope.launch {
            yimData.value = username?.let { repository.getYimData(username = it)}!!
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
        return yimData.value?.data?.payload?.data?.artistMap
    }
    
    /** Get [ListensPerDay] of a particular day.
     * @param day offset ([Int]) from 1st Jan */
    fun getListensOfDay(day : Int) : ListensPerDay? {
        if (day > 365){
            throw IllegalArgumentException()
        }
        return yimData.value?.data?.payload?.data?.listensPerDay?.get(day)
    }
    
    /** List of new releases of those artists that the user listens to.*/
    fun getNewReleasesOfTopArtists() : ArrayList<NewReleasesOfTopArtist>? {
        return yimData.value?.data?.payload?.data?.newReleasesOfTopArtists
    }
    
    /** The year of which the user listened most songs of. */
    fun getMostListenedYear() : Int? {
        val mapEntry = yimData.value?.data?.payload?.data?.mostListenedYear?.maxBy {
            it.value
        }
        return mapEntry?.value
    }
    
    /** The day user listens the most music, every week.*/
    fun getDayOfWeek() : String? {
        return yimData.value?.data?.payload?.data?.dayOfWeek
    }
    
    /** List of other ListenBrainz users with the same taste as user.
     *
     *  @return `null` for users with less listens.
     */
    fun getSimilarUsers(): Map<String, Double>? {
        return yimData.value?.data?.payload?.data?.similarUsers
    }
    
    /** List of top artists of which user listened songs of*/
    fun getTopArtists() : ArrayList<TopArtist>? {
        return yimData.value?.data?.payload?.data?.topArtists
    }
    
    /** Warning: Volatile fields (Might be null) :
     * @param caaId
     * @param caaReleaseMbid
     * @param releaseMbid
     */
    fun getTopRecordings() : ArrayList<TopRecording>? {
        return yimData.value?.data?.payload?.data?.topRecordings
    }
    
    /** Top releases user listened to.*/
    fun getTopReleases() : ArrayList<TopRelease>? {
        return yimData.value?.data?.payload?.data?.topReleases
    }
    
    /** Total of all artists the user listened to.*/
    fun getTotalArtistCount() : Int? {
        return yimData.value?.data?.payload?.data?.totalArtistsCount
    }
    
    fun getTotalListenCount() : Int? {
        return yimData.value?.data?.payload?.data?.totalListenCount
    }
    
    fun getTotalListeningTime() : Double? {
        return yimData.value?.data?.payload?.data?.totalListeningTime
    }
    
    fun getTotalNewArtistsDiscovered() : Int? {
        return yimData.value?.data?.payload?.data?.totalNewArtistsDiscovered
    }
    
    fun getTotalRecordingsCount() : Int? {
        return yimData.value?.data?.payload?.data?.totalRecordingsCount
    }
    
    fun getTotalReleasesCount() : Int? {
        return yimData.value?.data?.payload?.data?.totalReleasesCount
    }
}