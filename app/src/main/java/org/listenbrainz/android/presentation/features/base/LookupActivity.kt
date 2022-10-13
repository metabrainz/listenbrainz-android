package org.listenbrainz.android.presentation.features.base

import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.view.View
import androidx.viewpager2.widget.ViewPager2
import com.google.android.material.tabs.TabLayoutMediator
import org.listenbrainz.android.R
import org.listenbrainz.android.data.sources.api.entities.mbentity.MBEntity
import org.listenbrainz.android.databinding.ActivityLookupBinding
import org.listenbrainz.android.util.Resource

abstract class LookupActivity<T : MBEntity> : MusicBrainzActivity() {

    lateinit var binding: ActivityLookupBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLookupBinding.inflate(layoutInflater)
        setContentView(binding.root)
        supportActionBar!!.setBackgroundDrawable(ColorDrawable(resources.getColor(R.color.app_bg)))
        supportActionBar!!.setDisplayHomeAsUpEnabled(true)

        val pagerAdapter = ViewPagerAdapter(this, getFragmentsList())
        binding.pager.adapter = pagerAdapter
        binding.pager.orientation = ViewPager2.ORIENTATION_HORIZONTAL

        val titles = getTabsList()
        TabLayoutMediator(binding.tabs, binding.pager) { tab, position ->
            tab.text = resources.getText(titles[position])
        }.attach()

        binding.noResult.root.visibility = View.GONE
        binding.progressSpinner.root.visibility = View.VISIBLE
        binding.tabs.visibility = View.GONE
        binding.pager.visibility = View.GONE
    }

    abstract fun setData(data: T)

    abstract fun getFragmentsList(): List<MusicBrainzFragment>

    open fun getTabsList(): List<Int> = listOf(R.string.tab_info,
            R.string.tab_releases, R.string.tab_links, R.string.tab_edits)

    open fun processData(resource: Resource<T>) {
        binding.progressSpinner.root.visibility = View.GONE
        when (resource.status) {
            Resource.Status.SUCCESS -> {
                binding.noResult.root.visibility = View.GONE
                binding.tabs.visibility = View.VISIBLE
                binding.pager.visibility = View.VISIBLE
                setData(resource.data!!)
            }
            else -> {
                binding.noResult.root.visibility = View.VISIBLE
            }
        }
    }
}