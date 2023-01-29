package org.listenbrainz.android.presentation.features.adapters

import android.content.Context
import android.os.Build
import android.text.Html
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.cardview.widget.CardView
import androidx.core.text.HtmlCompat
import androidx.recyclerview.widget.RecyclerView
import org.listenbrainz.android.R
import org.listenbrainz.android.data.sources.api.entities.blog.Post

class BlogAdapter(private val context: Context, private val posts: ArrayList<Post>, private val clickListener: ClickListener) : RecyclerView.Adapter<BlogAdapter.ViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val v = LayoutInflater.from(parent.context).inflate(R.layout.card_blog, parent, false)
        return ViewHolder(v)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val post = posts[position]

        holder.body.text = when {
            Build.VERSION.SDK_INT >= Build.VERSION_CODES.N -> {
                Html.fromHtml(post.content, Html.FROM_HTML_MODE_COMPACT)
            }
            else -> {
                HtmlCompat.fromHtml(post.content, HtmlCompat.FROM_HTML_MODE_LEGACY)
            }
        }
        holder.heading.text = when {
            Build.VERSION.SDK_INT >= Build.VERSION_CODES.N -> {
                Html.fromHtml(post.title, Html.FROM_HTML_MODE_COMPACT)
            }
            else -> {
                HtmlCompat.fromHtml(post.title, HtmlCompat.FROM_HTML_MODE_LEGACY)
            }
        }
    }

    override fun getItemCount(): Int {
        return posts.size
    }

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView), View.OnClickListener, View.OnLongClickListener {

        val body: TextView = itemView.findViewById(R.id.tv_body)
        val heading: TextView = itemView.findViewById(R.id.tv_heading)
        val card: CardView = itemView.findViewById(R.id.card)

        override fun onClick(v: View) {
            clickListener.onUserClicked(adapterPosition)
        }

        override fun onLongClick(v: View?): Boolean {
            clickListener.onUserLongClicked(adapterPosition)
            return false
        }

        init {
            body.setOnClickListener(this)
            body.setOnLongClickListener(this)
        }
    }

    interface ClickListener {
        fun onUserClicked(position: Int)
        fun onUserLongClicked(position: Int)
    }
}