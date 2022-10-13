package org.listenbrainz.android.presentation.features.release_list

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import org.listenbrainz.android.R
import org.listenbrainz.android.data.sources.Constants
import org.listenbrainz.android.data.sources.api.entities.CoverArt
import org.listenbrainz.android.data.sources.api.entities.mbentity.Release
import org.listenbrainz.android.databinding.CardReleaseItemBinding
import org.listenbrainz.android.presentation.features.release.ReleaseActivity
import org.listenbrainz.android.presentation.features.release_list.ReleaseListAdapter.ReleaseItemViewHolder

class ReleaseListAdapter(val context: Activity, private val releaseList: List<Release>) : RecyclerView.Adapter<ReleaseItemViewHolder>() {

    // FIXME: Use Flow maybe
    private val viewModel: ReleaseListViewModel = ViewModelProvider((context as FragmentActivity?)!!)
            .get(ReleaseListViewModel::class.java)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ReleaseItemViewHolder {
        val inflater = parent.context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
        return ReleaseItemViewHolder(CardReleaseItemBinding.inflate(inflater, parent, false))
    }

    override fun onBindViewHolder(holder: ReleaseItemViewHolder, position: Int) {
        holder.bind(releaseList[position])
    }

    override fun getItemCount(): Int {
        return releaseList.size
    }

    override fun getItemId(position: Int): Long {
        return position.toLong()
    }

    override fun getItemViewType(position: Int): Int {
        return position
    }

    private fun setViewVisibility(text: String?, view: TextView) {
        if (text != null && !text.isEmpty() && !text.equals("null", ignoreCase = true)) {
            view.visibility = View.VISIBLE
            view.text = text
        } else view.visibility = View.GONE
    }

    inner class ReleaseItemViewHolder(var binding: CardReleaseItemBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(release: Release) {
            binding.releaseName.text = release.title
            setViewVisibility(release.disambiguation, binding.releaseDisambiguation)
            if (release.coverArt != null){
                setCoverArtView(release)
            }
            else {
                fetchCoverArtForRelease(release)
            }
            itemView.setOnClickListener { v: View ->
                val intent = Intent(v.context, ReleaseActivity::class.java)
                intent.putExtra(Constants.MBID, release.mbid)
                v.context.startActivity(intent)
            }
        }

        private fun setCoverArtView(release: Release?) {
            if (release?.coverArt != null && releaseList.contains(release)) {
                // TODO: Search for the first “FRONT” image to use it as cover
                val url = release.coverArt!!
                        .images[0]
                        .thumbnails!!
                        .small
                if (url != null && url.isNotEmpty()) {
                    Glide.with(context)
                            .load(Uri.parse(url))
                            .placeholder(R.drawable.link_discog)
                            .into(binding.releaseCoverArt)
                }
            }
        }

        private fun addCoverArt(coverArt: CoverArt?) {
            if (coverArt?.images != null && coverArt.images.isNotEmpty()) {
                val coverArtRelease = coverArt.release
                for (release in releaseList) {
                    if (coverArtRelease.endsWith(release.mbid!!)) {
                        release.coverArt = coverArt
                        setCoverArtView(release)
                        break
                    }
                }
            }
        }

        private fun fetchCoverArtForRelease(release: Release) {
            viewModel.fetchCoverArtForRelease(release).observeForever(this::addCoverArt)
        }
    }
}