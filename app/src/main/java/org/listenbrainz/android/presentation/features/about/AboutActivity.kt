package org.listenbrainz.android.presentation.features.about

import android.graphics.drawable.ColorDrawable
import android.net.Uri
import android.os.Bundle
import android.view.Menu
import org.listenbrainz.android.R
import org.listenbrainz.android.databinding.ActivityAboutBinding
import org.listenbrainz.android.presentation.features.base.MusicBrainzActivity

class AboutActivity : MusicBrainzActivity() {
    private var binding: ActivityAboutBinding? = null
    public override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAboutBinding.inflate(layoutInflater)
        setContentView(binding!!.root)
        supportActionBar!!.setBackgroundDrawable(ColorDrawable(resources.getColor(R.color.app_bg)))

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