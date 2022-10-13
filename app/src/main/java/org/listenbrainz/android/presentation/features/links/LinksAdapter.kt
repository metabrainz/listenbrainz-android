package org.listenbrainz.android.presentation.features.links

import android.content.Context
import android.content.Intent
import android.graphics.drawable.Drawable
import android.net.Uri
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import org.listenbrainz.android.R
import org.listenbrainz.android.data.sources.api.entities.Link
import org.listenbrainz.android.data.sources.api.entities.LinksClassifier
import org.listenbrainz.android.data.sources.api.entities.Url
import org.listenbrainz.android.databinding.ItemLinkBinding
import org.listenbrainz.android.presentation.features.links.LinksAdapter.LinkViewHolder

internal class LinksAdapter(private val context: Context, private val links: List<Link>) : RecyclerView.Adapter<LinkViewHolder>(), View.OnClickListener {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): LinkViewHolder {
        val layoutInflater = parent.context
                .getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
        return LinkViewHolder(ItemLinkBinding.inflate(layoutInflater, parent, false))
    }

    override fun onBindViewHolder(holder: LinkViewHolder, position: Int) {
        val type = links[position].type
        val drawable = getLinkImage(holder.itemView.context, type)
        holder.bind(drawable, type)
        holder.itemView.setTag(R.id.link_image, links[position].url)
        holder.itemView.setOnClickListener(this)
    }

    override fun getItemCount(): Int {
        return links.size
    }

    override fun onClick(v: View) {
        val url = v.getTag(R.id.link_image) as Url
        val intent = Intent()
        intent.action = Intent.ACTION_VIEW
        intent.data = Uri.parse(url.resource)
        context.startActivity(intent)
    }

    private fun getLinkImage(context: Context, type: String?): Drawable {
        val resources = context.resources
        return when (type) {
            LinksClassifier.YOUTUBE -> resources.getDrawable(R.drawable.youtube_logo)
            LinksClassifier.BANDCAMP -> resources.getDrawable(R.drawable.bandcamp_logo)
            LinksClassifier.DISCOGS -> resources.getDrawable(R.drawable.discogs_logo)
            LinksClassifier.IMDB -> resources.getDrawable(R.drawable.imdb_logo)
            LinksClassifier.VIAF -> resources.getDrawable(R.drawable.viaf_logo)
            LinksClassifier.MYSPACE -> resources.getDrawable(R.drawable.myspace_logo)
            else -> resources.getDrawable(R.drawable.link_generic)
        }
    }

    internal class LinkViewHolder(var binding: ItemLinkBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(drawable: Drawable?, text: String?) {
            binding.linkText.text = text
            binding.linkImage.setImageDrawable(drawable)
        }
    }
}