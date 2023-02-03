package org.listenbrainz.android.presentation.features.dashboard

import android.net.Uri
import android.os.Bundle
import android.view.Window
import androidx.appcompat.app.AppCompatActivity
import androidx.browser.customtabs.CustomTabsIntent
import org.listenbrainz.android.R

class DonateActivity : AppCompatActivity() {
    public override fun onCreate(savedInstanceState: Bundle?) {
        supportRequestWindowFeature(Window.FEATURE_INDETERMINATE_PROGRESS)
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