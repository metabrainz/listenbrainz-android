package org.listenbrainz.android.presentation.features.recording

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import org.listenbrainz.android.data.sources.api.entities.EntityUtils
import org.listenbrainz.android.data.sources.api.entities.mbentity.Recording
import org.listenbrainz.android.databinding.FragmentRecordingInfoBinding
import org.listenbrainz.android.presentation.features.base.MusicBrainzFragment
import org.listenbrainz.android.util.Resource

class RecordingInfoFragment : Fragment() {
    private var binding: FragmentRecordingInfoBinding? = null
    private val recordingViewModel: RecordingViewModel by activityViewModels()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        binding = FragmentRecordingInfoBinding.inflate(inflater, container, false)
        recordingViewModel.data.observe(viewLifecycleOwner) { setRecordingInfo(it) }
        return binding!!.root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        binding = null
    }

    private fun setRecordingInfo(resource: Resource<Recording>) {
        if (resource.status == Resource.Status.SUCCESS) {
            val recording = resource.data
            binding!!.recordingTitle.text = recording!!.title
            binding!!.recordingDuration.text = recording.duration
            binding!!.recordingArtist.text = EntityUtils.getDisplayArtist(recording.artistCredits)
        }
    }

    companion object : MusicBrainzFragment {
        override fun newInstance(): Fragment {
            return RecordingInfoFragment()
        }
    }
}