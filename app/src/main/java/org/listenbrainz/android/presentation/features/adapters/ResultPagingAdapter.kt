package org.listenbrainz.android.presentation.features.adapters

import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.AnimationUtils
import androidx.paging.PagingDataAdapter
import androidx.recyclerview.widget.DiffUtil
import org.listenbrainz.android.R
import org.listenbrainz.android.data.sources.Constants
import org.listenbrainz.android.data.sources.api.entities.mbentity.MBEntityType

class ResultPagingAdapter(diffCallback: DiffUtil.ItemCallback<ResultItem>, private val entity: MBEntityType) : PagingDataAdapter<ResultItem, ResultViewHolder>(diffCallback) {
    private var lastPosition = -1

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ResultViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_result, parent, false)
        return ResultViewHolder(view)
    }

    override fun onBindViewHolder(holder: ResultViewHolder, position: Int) {
        holder.bind(getItem(position))
        setAnimation(holder.itemView, position)
        holder.itemView.setOnClickListener { v: View -> onClick(v, position) }
    }

    private fun setAnimation(viewToAnimate: View, position: Int) {
        if (position > lastPosition) {
            val animation = AnimationUtils.loadAnimation(viewToAnimate.context, android.R.anim.slide_in_left)
            viewToAnimate.startAnimation(animation)
            lastPosition = position
        }
    }

    fun resetAnimation() {
        lastPosition = -1
    }

    private fun onClick(view: View, position: Int) {
        val intent = Intent(view.context, entity.typeActivityClass)
        intent.putExtra(Constants.MBID, getItem(position)!!.mBID)
        view.context.startActivity(intent)
    }
}