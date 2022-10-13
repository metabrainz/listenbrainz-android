package org.listenbrainz.android.presentation.features.about

import android.os.Bundle
import android.view.*
import androidx.fragment.app.Fragment
import org.listenbrainz.android.App
import org.listenbrainz.android.R
import org.listenbrainz.android.databinding.FragmentAboutBinding

class AboutFragment : Fragment() {
    private var binding: FragmentAboutBinding? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHasOptionsMenu(true)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        binding = FragmentAboutBinding.inflate(inflater, container, false)
        binding!!.aboutText.setAsset("about.html")
        return binding!!.root
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        val version = getText(R.string.version_text).toString() + " " + App.version
        binding!!.versionText.text = version
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.fragment_about, menu)
    }
}