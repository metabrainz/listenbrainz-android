package org.listenbrainz.android.presentation.features.release_group

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import org.listenbrainz.android.data.sources.api.entities.EntityUtils
import org.listenbrainz.android.data.sources.api.entities.WikiSummary
import org.listenbrainz.android.data.sources.api.entities.mbentity.ReleaseGroup
import org.listenbrainz.android.databinding.CardReleaseGroupInfoBinding
import org.listenbrainz.android.presentation.features.base.MusicBrainzFragment
import org.listenbrainz.android.util.Resource

class ReleaseGroupInfoFragment : Fragment() {
    private var binding: CardReleaseGroupInfoBinding? = null
    private val releaseGroupViewModel: ReleaseGroupViewModel by activityViewModels()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        binding = CardReleaseGroupInfoBinding.inflate(inflater, container, false)
        releaseGroupViewModel.data.observe(viewLifecycleOwner) { setReleaseGroupInfo(it) }
        releaseGroupViewModel.wikiData.observe(viewLifecycleOwner) { setWiki(it) }
        return binding!!.root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        binding = null
    }

    private fun setWiki(wikiSummaryResource: Resource<WikiSummary>?) {
        if (wikiSummaryResource != null && wikiSummaryResource.status == Resource.Status.SUCCESS) {
            val wiki = wikiSummaryResource.data
            val wikiText = wiki!!.extract
            if (wikiText != null && !wikiText.isEmpty()) {
                showWikiCard()
                binding!!.wikiSummary.text = wikiText
            } else hideWikiCard()
        } else hideWikiCard()
    }

    private fun showWikiCard() {
        binding!!.cardView.visibility = View.VISIBLE
    }

    private fun hideWikiCard() {
        binding!!.cardView.visibility = View.GONE
    }

    private fun setReleaseGroupInfo(resource: Resource<ReleaseGroup>?) {
        if (resource != null && resource.status == Resource.Status.SUCCESS) {
            val releaseGroup = resource.data
            binding!!.releaseGroupTitle.text = releaseGroup!!.title
            binding!!.releaseGroupArtist.text = EntityUtils.getDisplayArtist(releaseGroup.artistCredits)
        }
    }

    companion object : MusicBrainzFragment {
        override fun newInstance(): Fragment {
            return ReleaseGroupInfoFragment()
        }
    }
}