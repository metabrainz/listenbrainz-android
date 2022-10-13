package org.listenbrainz.android.presentation.features.userdata

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import org.listenbrainz.android.data.sources.api.entities.mbentity.MBEntity
import org.listenbrainz.android.data.sources.api.entities.userdata.Tag
import org.listenbrainz.android.data.sources.api.entities.userdata.UserTag
import org.listenbrainz.android.databinding.FragmentUserDataBinding
import org.listenbrainz.android.presentation.features.base.MusicBrainzFragment
import java.util.*

class UserDataFragment : Fragment() {

    private var binding: FragmentUserDataBinding? = null
    private lateinit var tagsAdapter: TagAdapter
    private lateinit var userTagsAdapter: UserTagAdapter
    private val tags: MutableList<Tag> = ArrayList()
    private val userTags: MutableList<UserTag> = ArrayList()
    private val viewModel: UserViewModel by activityViewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        tagsAdapter = TagAdapter(tags)
        userTagsAdapter = UserTagAdapter(userTags)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View {
        binding = FragmentUserDataBinding.inflate(inflater, container, false)
        viewModel.userData.observe(viewLifecycleOwner, { entity: MBEntity -> updateData(entity) })
        bindViews()
        return binding!!.root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        binding = null
    }

    private fun bindViews() {
        binding!!.tagsList.layoutManager = LinearLayoutManager(activity)
        binding!!.tagsList.adapter = tagsAdapter
        binding!!.userTagsList.layoutManager = LinearLayoutManager(activity)
        binding!!.userTagsList.adapter = userTagsAdapter
        val dividerItemDecoration = DividerItemDecoration(requireActivity(), DividerItemDecoration.VERTICAL)
        binding!!.tagsList.addItemDecoration(dividerItemDecoration)
        binding!!.userTagsList.addItemDecoration(dividerItemDecoration)
        binding!!.noRating.visibility = View.GONE
        binding!!.noUserRating.visibility = View.GONE
        binding!!.noUserTag.visibility = View.GONE
        binding!!.noTag.visibility = View.GONE
    }

    private fun displayRating(entity: MBEntity?) {
        if (entity?.rating != null && entity.rating!!.value != 0f) binding!!.rating.rating = entity.rating!!.value else {
            binding!!.noRating.visibility = View.VISIBLE
            binding!!.rating.visibility = View.GONE
        }
        if (entity?.userRating != null && entity.userRating!!.value != 0f) binding!!.userRating.rating = entity.userRating!!.value else {
            binding!!.noUserRating.visibility = View.VISIBLE
            binding!!.userRating.visibility = View.GONE
        }
    }

    private fun addTags(entity: MBEntity?) {
        if (entity?.tags != null) {
            tags.clear()
            tags.addAll(entity.tags)
            tagsAdapter.notifyDataSetChanged()
            if (tags.size == 0) {
                binding!!.noTag.visibility = View.VISIBLE
                binding!!.tagsList.visibility = View.GONE
            }
        } else {
            binding!!.noTag.visibility = View.VISIBLE
            binding!!.tagsList.visibility = View.GONE
        }
        if (entity != null) {
            userTags.clear()
            userTags.addAll(entity.userTags)
            userTagsAdapter.notifyDataSetChanged()
            if (userTags.size == 0) {
                binding!!.noUserTag.visibility = View.VISIBLE
                binding!!.userTagsList.visibility = View.GONE
            }
        } else {
            binding!!.noUserTag.visibility = View.VISIBLE
            binding!!.userTagsList.visibility = View.GONE
        }
    }

    private fun updateData(entity: MBEntity) {
        addTags(entity)
        displayRating(entity)
    }

    companion object : MusicBrainzFragment {
        override fun newInstance(): Fragment {
            return UserDataFragment()
        }
    }
}