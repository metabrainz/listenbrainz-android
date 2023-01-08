package org.listenbrainz.android.presentation.features.release

import android.os.Bundle
import android.os.Handler
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import com.google.android.material.tabs.TabLayoutMediator
import org.listenbrainz.android.data.sources.api.entities.CoverArt
import org.listenbrainz.android.data.sources.api.entities.mbentity.Release
import org.listenbrainz.android.databinding.CardReleaseInfoBinding
import org.listenbrainz.android.presentation.features.base.MusicBrainzFragment
import org.listenbrainz.android.util.Resource
import org.listenbrainz.android.util.Utils
import java.util.*

class ReleaseInfoFragment : Fragment() {
    private var binding: CardReleaseInfoBinding? = null
    private val viewModel: ReleaseViewModel by activityViewModels()

    private lateinit var slideshowAdapter: CoverArtSlideshowAdapter
    private val urls = ArrayList<String>()
    private var releaseMBID: String? = null

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        binding = CardReleaseInfoBinding.inflate(inflater, container, false)
        viewModel.data.observe(viewLifecycleOwner) { setData(it) }
        viewModel.coverArtData.observe(viewLifecycleOwner) { setCoverArt(it) }
        slideshowAdapter = CoverArtSlideshowAdapter(urls)

        binding!!.viewpagerSlideshow.adapter = slideshowAdapter

        TabLayoutMediator(binding!!.tabIndicator, binding!!.viewpagerSlideshow) { _, _ -> }.attach()
        return binding!!.root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        binding = null
    }

    private fun setData(resource: Resource<Release>) {
        if (resource.status == Resource.Status.SUCCESS) {
            val release = resource.data
            if (release!!.title != null && release.title!!.isNotEmpty())
                binding!!.releaseTitle.text = release.title
            if (release.barcode != null && release.barcode!!.isNotEmpty()) {
                binding!!.releaseBarcode.text = release.barcode
                binding!!.releaseBarcode.visibility = View.VISIBLE
            }
            else{
                binding!!.releaseBarcode.visibility = View.GONE
            }
            releaseMBID = release.mbid
            if (release.status != null && release.status!!.isNotEmpty())
                binding!!.releaseStatus.text = release.status
            if (release.textRepresentation != null && release.textRepresentation!!.language != null)
                binding!!.releaseLanguage.text = release.textRepresentation!!.language
        }
    }

    private fun setCoverArt(coverArt: CoverArt?) {
        if (coverArt != null && coverArt.images.isNotEmpty()) {
            urls.clear()
            urls.addAll(listOf(coverArt.images[0].image))
            slideshowAdapter.notifyDataSetChanged()
            startAutoSlide()
        }
    }

    private fun startAutoSlide() {
        val NUM_PAGES = urls.size
        val handler = Handler()
        val runnable: Runnable = object : Runnable {
            override fun run() {
                if (binding != null) {
                    var position = binding!!.viewpagerSlideshow.currentItem
                    if (position == NUM_PAGES - 1) position = 0 else position++
                    binding!!.viewpagerSlideshow.setCurrentItem(position, true)
                    handler.postDelayed(this, 10000)
                }
            }
        }
        handler.postDelayed(runnable, 10000)
    }

    companion object : MusicBrainzFragment {
        override fun newInstance(): Fragment {
            return ReleaseInfoFragment()
        }
    }
}