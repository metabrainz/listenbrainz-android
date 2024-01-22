package org.listenbrainz.android.viewmodel

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.util.Log
import android.widget.Toast
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.caverock.androidsvg.SVG
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.Flow
import org.listenbrainz.android.model.SocialData
import org.listenbrainz.android.model.yimdata.*
import org.listenbrainz.android.repository.preferences.AppPreferences
import org.listenbrainz.android.repository.social.SocialRepository
import org.listenbrainz.android.repository.yim23.Yim23Repository
import org.listenbrainz.android.util.Resource
import org.listenbrainz.android.util.Utils.saveBitmap
import java.net.URL
import javax.inject.Inject

@HiltViewModel
class Yim23ViewModel @Inject constructor(
    private val repository: Yim23Repository,
    private val socialRepository: SocialRepository,
    private val appPreferences: AppPreferences
) : ViewModel() {
    // Yim data resource
    var yimData:
            MutableState<
                    Resource<Yim23Payload>
                    >
            = mutableStateOf(Resource.loading())
    val loginFlow = appPreferences.getLoginStatusFlow()
    var themeType : MutableState<Yim23ThemeData> = mutableStateOf(Yim23ThemeData.GREEN)
    val followers : MutableState<Resource<SocialData>?> =  mutableStateOf(Resource.loading())

    init {
        getData()
        getFollowers()
    }

    private fun getData() {
        viewModelScope.launch {
            val response = repository.getYimData(username = getUsername())
            when (response.status){
                Resource.Status.SUCCESS -> yimData.value = response
                Resource.Status.LOADING -> yimData.value = Resource.loading()
                Resource.Status.FAILED -> yimData.value = Resource.failure()
            }
        }
    }

    // Username related functions
    suspend fun getUsername() : String {
        return appPreferences.username.get()
    }

    fun getUsernameFlow() : Flow<String> {
        return appPreferences.username.getFlow()
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

    fun getMostListenedMonth () : Pair<String , Int> {
        var jan: Int = 0
        var feb: Int = 0
        var mar: Int = 0
        var apr: Int = 0
        var may: Int = 0
        var jun: Int = 0
        var jul: Int = 0
        var aug: Int = 0
        var sep: Int = 0
        var oct: Int = 0
        var nov: Int = 0
        var dec: Int = 0

        yimData.value.data?.payload?.data?.listensPerDay?.forEach {
            if (it.timeRange.lowercase().contains("jan")) {
                jan += (it.listenCount)
            }
            if (it.timeRange.lowercase().contains("feb")) {
                feb += (it.listenCount)
            }
            if (it.timeRange.lowercase().contains("mar")) {
                mar += (it.listenCount)
            }
            if (it.timeRange.lowercase().contains("apr")) {
                apr += (it.listenCount)
            }
            if (it.timeRange.lowercase().contains("may")) {
                may += (it.listenCount)
            }
            if (it.timeRange.lowercase().contains("jun")) {
                jun += (it.listenCount)
            }
            if (it.timeRange.lowercase().contains("jul")) {
                jul += (it.listenCount)
            }
            if (it.timeRange.lowercase().contains("aug")) {
                aug += (it.listenCount)
            }
            if (it.timeRange.lowercase().contains("sept")) {
                sep += (it.listenCount)
            }
            if (it.timeRange.lowercase().contains("oct")) {
                oct += (it.listenCount)
            }
            if (it.timeRange.lowercase().contains("nov")) {
                nov += (it.listenCount)
            }
            if (it.timeRange.lowercase().contains("dec")) {
                dec += (it.listenCount)
            }
        }

        val list: List<Int> = listOf(jan, feb, mar, apr, may, jun, jul, aug, sep, oct, nov, dec)

        var max_list = 0
        var index = 0
        for (i in 1..12) {
            if(list[i-1] > max_list){
                max_list = list[i-1]
                index = i
            }
        }

        val month : String = when (index) {
            1 -> "January"
            2 -> "February"
            3 -> "March"
            4 -> "April"
            5 -> "May"
            6 -> "June"
            7 -> "July"
            8 -> "August"
            9 -> "September"
            10 -> "October"
            11 -> "November"
            12 -> "December"
            else -> ""
        }

        val ans : Pair<String , Int> = Pair(month , max_list)
        return ans
    }

    /** List of new releases of those artists that the user listens to.*/
    fun getNewReleasesOfTopArtists() : ArrayList<NewReleasesOfTopArtist>? {
        return yimData.value.data?.payload?.data?.newReleasesOfTopArtists
    }

    /** The year of which the user listened most songs of. */
    fun getYearListens() : Map<String , Int> {
        return yimData.value.data?.payload?.data?.mostListenedYear!!
    }

    fun getMostListenedYear() : Map.Entry<String , Int>? {
        val mapEntry = yimData.value.data?.payload?.data?.mostListenedYear?.maxBy {
            it.value
        }
        return mapEntry
    }

    /** The day user listens the most music, every week.*/
    fun getDayOfWeek() : String {
        return yimData.value.data?.payload?.data?.dayOfWeek.toString()
    }

    /** List of other ListenBrainz users with the same taste as user.
     *
     *  @return `null` for users with less listens.
     */
    fun getSimilarUsers(): List<Pair<String, Double>>? {
        val list = yimData.value.data?.payload?.data?.similarUsers?.toList()
        return list?.sortedByDescending {
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
    fun getTopReleases() : ArrayList<TopReleaseYim23>? {
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


    fun getTopGenres () : List<TopGenre> {
        return yimData.value.data?.payload?.data?.topGenres!!.toList()
    }

    fun getTopDiscoveries () : Yim23TopDiscoveries {
        return yimData.value.data?.payload?.data?.topDiscoveriesPlaylist!!
    }

    fun getMissedSongs () : Yim23TopDiscoveries {
        return yimData.value.data?.payload?.data?.topMissedRecordings!!
    }

    /** Shareable types : "stats", "artists", "albums", "tracks", "discovery-playlist", "missed-playlist".*/
    fun saveSharableImage(sharableType: String, context: Context)
    {
        viewModelScope.launch(Dispatchers.IO) {
            val bitmap: Bitmap = Bitmap.createBitmap(924,924,Bitmap.Config.ARGB_8888)
            val canvas = Canvas(bitmap)
            val imageURL = "https://api.listenbrainz.org/1/art/year-in-music/2023/${getUsername()}?image=$sharableType"

            try {
                // Download Image from URL
                URL(imageURL).openStream().use {
                    // Decode Bitmap
                    SVG.getFromInputStream(it).renderToCanvas(canvas)
                }

                saveBitmap(
                    context = context,
                    bitmap = bitmap,
                    format = Bitmap.CompressFormat.PNG,
                    displayName = "${getUsername()}'s $sharableType",
                    launchShareIntent = true
                )

            }catch (e: Exception){
                withContext(Dispatchers.Main){
                    Toast.makeText(context, "Something went wrong.", Toast.LENGTH_SHORT).show()
                }
                e.localizedMessage?.let { Log.e("YimShareError", it) }
            }

        }
    }

    fun getFollowers() {
        viewModelScope.launch {
            val response = socialRepository.getFollowers(getUsername())
            when (response.status) {
                Resource.Status.SUCCESS -> followers.value = response
                Resource.Status.FAILED -> followers.value = Resource.failure()
                Resource.Status.LOADING -> followers.value = Resource.loading()
            }
        }
    }
}