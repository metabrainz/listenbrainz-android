package org.listenbrainz.android.presentation.features.label

import android.net.Uri
import android.os.Bundle
import androidx.activity.viewModels
import androidx.lifecycle.SavedStateViewModelFactory
import dagger.hilt.android.AndroidEntryPoint
import org.listenbrainz.android.App
import org.listenbrainz.android.data.sources.Constants
import org.listenbrainz.android.data.sources.api.entities.mbentity.Label
import org.listenbrainz.android.presentation.features.base.LookupActivity
import org.listenbrainz.android.presentation.features.base.MusicBrainzFragment
import org.listenbrainz.android.presentation.features.links.LinksFragment
import org.listenbrainz.android.presentation.features.links.LinksViewModel
import org.listenbrainz.android.presentation.features.release_list.ReleaseListFragment
import org.listenbrainz.android.presentation.features.release_list.ReleaseListViewModel
import org.listenbrainz.android.presentation.features.userdata.UserDataFragment
import org.listenbrainz.android.presentation.features.userdata.UserViewModel

@AndroidEntryPoint
class LabelActivity : LookupActivity<Label>() {

    private val labelViewModel: LabelViewModel by viewModels()
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
        if (mbid != null && mbid.isNotEmpty()) {
            labelViewModel.mbid.value = mbid
        }
        labelViewModel.data.observe(this) { processData(it) }
    }

    override fun setData(data: Label) {
        supportActionBar?.title = data.name
        userViewModel.setUserData(data)
        linksViewModel.setData(data.relations)
        releaseListViewModel.setReleases(data.releases)
    }

    override fun getFragmentsList(): List<MusicBrainzFragment> = listOf(LabelInfoFragment,
            ReleaseListFragment, LinksFragment, UserDataFragment)

    override fun getBrowserURI(): Uri {
        val mbid = labelViewModel.mbid.value ?: return Uri.EMPTY
        return Uri.parse(App.WEBSITE_BASE_URL + "label/" + mbid)
    }

}