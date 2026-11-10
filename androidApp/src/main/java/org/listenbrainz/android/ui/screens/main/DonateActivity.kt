package org.listenbrainz.android.ui.screens.main

import android.net.Uri
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.browser.customtabs.CustomTabsIntent
import org.listenbrainz.android.R

class DonateActivity : ComponentActivity() {
    public override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val customTabsIntent = CustomTabsIntent.Builder()
                .setToolbarColor(resources.getColor(R.color.colorPrimaryDark))
                .setShowTitle(true)
                .build()
        customTabsIntent.launchUrl(this,
                Uri.parse("https://metabrainz.org/donate"))
    }

    override fun onResume() {
        finish()
        super.onResume()
    }
}