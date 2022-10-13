package org.listenbrainz.android.presentation.features.artist

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import org.listenbrainz.android.R
import org.listenbrainz.android.data.sources.api.entities.WikiSummary
import org.listenbrainz.android.data.sources.api.entities.mbentity.Artist
import org.listenbrainz.android.databinding.FragmentBioBinding
import org.listenbrainz.android.presentation.features.base.MusicBrainzFragment
import org.listenbrainz.android.util.Resource

class ArtistBioFragment : Fragment() {
    private var binding: FragmentBioBinding? = null

    private val artistViewModel: ArtistViewModel by activityViewModels()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        binding = FragmentBioBinding.inflate(inflater, container, false)
        artistViewModel.data.observe(viewLifecycleOwner, { setArtistInfo(it) })
        artistViewModel.wikiData.observe(viewLifecycleOwner, { setWiki(it) })
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
            if (wikiText != null && wikiText.isNotEmpty()) {
                showWikiCard()
                binding!!.wikiSummary.text = wikiText
            }
            else {
                hideWikiCard()
            }
        }
        else {
            hideWikiCard()
        }
    }

    private fun showWikiCard() {
        binding!!.wikiSummary.visibility = View.VISIBLE
        binding!!.poweredByWikipedia.visibility = View.VISIBLE
    }

    private fun hideWikiCard() {
        binding!!.wikiSummary.visibility = View.GONE
        binding!!.poweredByWikipedia.visibility = View.GONE
    }

    private fun setArtistInfo(resource: Resource<Artist>) {
        if (resource.status == Resource.Status.SUCCESS) {
            val artist = resource.data

            if (artist!!.type != null && artist.type!!.isNotEmpty()) {
                binding!!.artistType.text = artist.type
                if(artist.type == "Group"){
                    binding!!.imageView.setImageResource(R.drawable.ic_group)
                }
                else{
                    binding!!.imageView.setImageResource(R.drawable.ic_user)
                }
            }
            binding!!.artistName.text = artist.name
            if (artist.gender != null && artist.gender!!.isNotEmpty()) {
                binding!!.artistGender.text = artist.gender
            }
            if (artist.area != null && artist.area!!.name != null) {
                binding!!.artistArea.text = artist.area!!.name
            }
            if (artist.lifeSpan != null) {
                binding!!.lifeSpan.text = artist.lifeSpan!!.timePeriod
            }
        }
    }

    companion object : MusicBrainzFragment {
        override fun newInstance(): Fragment {
            return ArtistBioFragment()
        }
    }

}