package org.listenbrainz.android.ui.screens.about

import android.graphics.drawable.ColorDrawable
import android.net.Uri
import android.os.Bundle
import android.view.Menu
import dagger.hilt.android.AndroidEntryPoint
import org.listenbrainz.android.R
import org.listenbrainz.android.databinding.ActivityAboutBinding
import org.listenbrainz.android.repository.AppPreferences
import org.listenbrainz.android.ui.components.ListenBrainzActivity
import javax.inject.Inject

@AndroidEntryPoint
class AboutActivity : ListenBrainzActivity() {
    private var binding: ActivityAboutBinding? = null
    @Inject
    lateinit var appPreferences: AppPreferences

    public override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAboutBinding.inflate(layoutInflater)
        setContentView(binding!!.root)

        binding?.aboutText?.setAsset("about.html")
        val version = getText(R.string.version_text).toString() + " " + appPreferences.version
        binding?.versionText?.text = version

        supportActionBar?.setBackgroundDrawable(ColorDrawable(resources.getColor(R.color.app_bg)))

        supportActionBar?.setDisplayHomeAsUpEnabled(true)
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.about, menu)
        return true
    }


    override fun getBrowserURI(): Uri {
        return Uri.EMPTY
    }
}