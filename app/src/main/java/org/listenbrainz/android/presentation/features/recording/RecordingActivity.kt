package org.listenbrainz.android.presentation.features.recording

import android.net.Uri
import android.os.Bundle
import androidx.activity.viewModels
import androidx.lifecycle.SavedStateViewModelFactory
import dagger.hilt.android.AndroidEntryPoint
import org.listenbrainz.android.App
import org.listenbrainz.android.data.sources.Constants
import org.listenbrainz.android.data.sources.api.entities.mbentity.Recording
import org.listenbrainz.android.presentation.features.base.LookupActivity
import org.listenbrainz.android.presentation.features.base.MusicBrainzFragment
import org.listenbrainz.android.presentation.features.links.LinksFragment
import org.listenbrainz.android.presentation.features.links.LinksViewModel
import org.listenbrainz.android.presentation.features.release_list.ReleaseListFragment
import org.listenbrainz.android.presentation.features.release_list.ReleaseListViewModel
import org.listenbrainz.android.presentation.features.userdata.UserDataFragment
import org.listenbrainz.android.presentation.features.userdata.UserViewModel

@AndroidEntryPoint
class RecordingActivity : LookupActivity<Recording>() {

    private val recordingViewModel: RecordingViewModel by viewModels()
    private val userViewModel: UserViewModel by viewModels {
        SavedStateViewModelFactory(application, this)
    }
    private val linksViewModel: LinksViewModel by viewModels {
        SavedStateViewModelFactory(application, this)
    }
    private val releaseListViewModel: ReleaseListViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val mbid = intent.getStringExtra(Constants.MBID)
        if (mbid != null && mbid.isNotEmpty())
            recordingViewModel.mbid.value = mbid
        recordingViewModel.data.observe(this) { processData(it) }
    }

    override fun setData(data: Recording) {
        supportActionBar?.title = data.title

        userViewModel.setUserData(data)
        linksViewModel.setData(data.relations)
        releaseListViewModel.setReleases(data.releases)
    }

    override fun getFragmentsList(): List<MusicBrainzFragment> = listOf(RecordingInfoFragment,
            ReleaseListFragment, LinksFragment, UserDataFragment)

    override fun getBrowserURI(): Uri {
        val mbid = recordingViewModel.mbid.value ?: return Uri.EMPTY
        return Uri.parse(App.WEBSITE_BASE_URL + "recording/" + mbid)
    }

}