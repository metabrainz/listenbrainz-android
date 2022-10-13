package org.listenbrainz.android.presentation.features.release_list

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.GridLayoutManager
import org.listenbrainz.android.data.sources.api.entities.mbentity.Release
import org.listenbrainz.android.databinding.FragmentReleaseListBinding
import org.listenbrainz.android.presentation.features.base.MusicBrainzFragment
import java.util.*

class ReleaseListFragment : Fragment() {
    private var binding: FragmentReleaseListBinding? = null

    private lateinit var adapter: ReleaseListAdapter
    private lateinit var releaseList: MutableList<Release>
    private val viewModel: ReleaseListViewModel by activityViewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        releaseList = ArrayList()
        adapter = ReleaseListAdapter(requireActivity(), releaseList)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        binding = FragmentReleaseListBinding.inflate(inflater, container, false)
        binding!!.recyclerView.layoutManager = GridLayoutManager(binding!!.root.context,2)
        binding!!.recyclerView.adapter = adapter
        val itemDecoration = DividerItemDecoration(binding!!.root.context,
                DividerItemDecoration.VERTICAL)
        binding!!.recyclerView.addItemDecoration(itemDecoration)
        viewModel.releaseList.observe(viewLifecycleOwner) { setReleases(it) }
        return binding!!.root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        binding = null
    }

    private fun setReleases(releases: List<Release>?) {
        // TODO: Observe artistData LiveData, instead of requesting the artist sync
        // TODO: Use DiffUtil to avoid overheads
        if (releases != null) {
            releaseList.clear()
            releaseList.addAll(releases)
            adapter.notifyDataSetChanged()
        }
    }

    companion object : MusicBrainzFragment {
        override fun newInstance(): Fragment {
            return ReleaseListFragment()
        }
    }
}