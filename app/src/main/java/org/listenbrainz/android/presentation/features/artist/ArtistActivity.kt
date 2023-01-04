package org.listenbrainz.android.presentation.features.artist

import android.net.Uri
import android.os.Bundle
import androidx.activity.viewModels
import androidx.lifecycle.SavedStateViewModelFactory
import dagger.hilt.android.AndroidEntryPoint
import org.listenbrainz.android.App
import org.listenbrainz.android.data.sources.Constants
import org.listenbrainz.android.data.sources.api.entities.mbentity.Artist
import org.listenbrainz.android.presentation.features.base.LookupActivity
import org.listenbrainz.android.presentation.features.links.LinksFragment
import org.listenbrainz.android.presentation.features.links.LinksViewModel
import org.listenbrainz.android.presentation.features.release_list.ReleaseListFragment
import org.listenbrainz.android.presentation.features.release_list.ReleaseListViewModel
import org.listenbrainz.android.presentation.features.userdata.UserDataFragment
import org.listenbrainz.android.presentation.features.userdata.UserViewModel

/**
 * Activity that retrieves and displays information about an artist given an
 * artist MBID.
 */
@AndroidEntryPoint
class ArtistActivity : LookupActivity<Artist>() {

    private val artistViewModel: ArtistViewModel by viewModels()
    private val releaseListViewModel: ReleaseListViewModel by viewModels()
    private val linksViewModel: LinksViewModel by viewModels {
        SavedStateViewModelFactory(application, this)
    }
    private val userViewModel: UserViewModel by viewModels {
        SavedStateViewModelFactory(application, this)
    }

    public override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val mbid = intent.getStringExtra(Constants.MBID)
        if (mbid != null && mbid.isNotEmpty())
            artistViewModel.mbid.value = mbid
        artistViewModel.data.observe(this) { processData(it) }
    }

    override fun getBrowserURI(): Uri {
        val mbid = artistViewModel.mbid.value ?: return Uri.EMPTY
        return Uri.parse(App.WEBSITE_BASE_URL + "artist/" + mbid)
    }

    override fun setData(data: Artist) {
        supportActionBar?.title = data.name
        userViewModel.setUserData(data)
        releaseListViewModel.setReleases(data.releases)
        linksViewModel.setData(data.relations)
    }

    override fun getFragmentsList() = listOf(ArtistBioFragment,
            ReleaseListFragment, LinksFragment, UserDataFragment)
}