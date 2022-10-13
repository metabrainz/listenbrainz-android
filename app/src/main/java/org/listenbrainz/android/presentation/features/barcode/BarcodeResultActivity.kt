package org.listenbrainz.android.presentation.features.barcode

import android.content.Intent
import android.graphics.drawable.ColorDrawable
import android.net.Uri
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import dagger.hilt.android.AndroidEntryPoint
import org.listenbrainz.android.presentation.features.base.MusicBrainzActivity
import org.listenbrainz.android.App
import org.listenbrainz.android.R
import org.listenbrainz.android.data.sources.Constants
import org.listenbrainz.android.data.sources.api.entities.mbentity.Release
import org.listenbrainz.android.databinding.ActivityBarcodeResultBinding
import org.listenbrainz.android.presentation.features.barcode.BarcodeViewModel
import org.listenbrainz.android.presentation.features.release.ReleaseActivity
import org.listenbrainz.android.presentation.features.release_list.ReleaseListAdapter
import org.listenbrainz.android.util.Resource
import java.util.*

@AndroidEntryPoint
class BarcodeResultActivity : MusicBrainzActivity() {
    private lateinit var binding: ActivityBarcodeResultBinding
    private val releases: MutableList<Release> = ArrayList()
    private var viewModel: BarcodeViewModel? = null
    private var adapter: ReleaseListAdapter? = null
    private var barcode: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityBarcodeResultBinding.inflate(layoutInflater)
        setContentView(binding.root)
        supportActionBar!!.setBackgroundDrawable(ColorDrawable(resources.getColor(R.color.app_bg)))
        supportActionBar!!.setDisplayHomeAsUpEnabled(true)

        adapter = ReleaseListAdapter(this, releases)
        binding.recyclerView.layoutManager = LinearLayoutManager(this)
        val itemDecoration = DividerItemDecoration(this, DividerItemDecoration.VERTICAL)
        binding.recyclerView.addItemDecoration(itemDecoration)
        binding.recyclerView.adapter = adapter
        binding.recyclerView.visibility = View.GONE
        binding.noResult.root.visibility = View.GONE
        viewModel = ViewModelProvider(this).get(BarcodeViewModel::class.java)
        barcode = intent.getStringExtra("barcode")

        when {
            barcode != null && barcode!!.isNotEmpty() -> {
                viewModel!!.fetchReleasesWithBarcode(barcode!!).observe(this, { resource: Resource<List<Release>> -> handleResult(resource) })
                binding.progressSpinner.root.visibility = View.VISIBLE
            }
            else -> {
                binding.progressSpinner.root.visibility = View.GONE
                Toast.makeText(this, "Unknown barcode error", Toast.LENGTH_LONG).show()
                finish()
            }
        }
    }

    private fun handleResult(resource: Resource<List<Release>>) {
        releases.clear()
        binding.progressSpinner.root.visibility = View.GONE
        when {
            resource.status === Resource.Status.SUCCESS -> {
                releases.addAll(resource.data!!)
                when (releases.size) {
                    0 -> binding.noResult.root.visibility = View.VISIBLE
                    1 -> {
                        val intent = Intent(this, ReleaseActivity::class.java)
                        intent.putExtra(Constants.MBID, releases[0].mbid)
                        startActivity(intent)
                        finish()
                    }
                    else -> showMultipleReleases()
                }
            }
        }
    }

    private fun showMultipleReleases() {
        adapter!!.notifyDataSetChanged()
        binding.recyclerView.visibility = View.VISIBLE
    }

    override fun getBrowserURI(): Uri {
        return Uri.parse(App.WEBSITE_BASE_URL + "search?type=release&advanced=1&query=barcode:" + barcode)
    }
}