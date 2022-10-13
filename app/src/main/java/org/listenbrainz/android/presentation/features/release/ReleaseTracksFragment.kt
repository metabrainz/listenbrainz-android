package org.listenbrainz.android.presentation.features.release

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.ViewCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import org.listenbrainz.android.data.sources.api.entities.Media
import org.listenbrainz.android.data.sources.api.entities.mbentity.Release
import org.listenbrainz.android.databinding.FragmentTracklistBinding
import org.listenbrainz.android.presentation.features.base.MusicBrainzFragment
import org.listenbrainz.android.util.Resource
import java.util.*

class ReleaseTracksFragment : Fragment() {
    private var binding: FragmentTracklistBinding? = null

    private val viewModel: ReleaseViewModel by activityViewModels()

    private lateinit var adapter: ReleaseTrackAdapter
    private lateinit var mediaList: MutableList<Media>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        mediaList = ArrayList()
        adapter = ReleaseTrackAdapter(mediaList)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        binding = FragmentTracklistBinding.inflate(inflater, container, false)
        binding!!.trackList.layoutManager = LinearLayoutManager(binding!!.root.context)
        val itemDecoration = DividerItemDecoration(binding!!.root.context, DividerItemDecoration.VERTICAL)
        binding!!.trackList.addItemDecoration(itemDecoration)
        binding!!.trackList.adapter = adapter
        ViewCompat.setNestedScrollingEnabled(binding!!.trackList, false)
        viewModel.data.observe(viewLifecycleOwner) { setTracks(it) }
        return binding!!.root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        binding = null
    }

    private fun setTracks(resource: Resource<Release>) {
        if (resource.status == Resource.Status.SUCCESS) {
            val release = resource.data
            if (release!!.media != null && release.media!!.isNotEmpty()) {
                mediaList.clear()
                mediaList.addAll(release.media!!)
                adapter.notifyDataSetChanged()
            }
        }
    }

    companion object : MusicBrainzFragment {
        override fun newInstance(): Fragment {
            return ReleaseTracksFragment()
        }
    }
}