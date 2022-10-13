package org.listenbrainz.android.presentation.features.adapters

import android.view.View
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import org.listenbrainz.android.databinding.ItemResultBinding

class ResultViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

    private val binding: ItemResultBinding = ItemResultBinding.bind(itemView)

    fun bind(item: ResultItem?) {
        setViewVisibility(item!!.name, binding.itemName)
        setViewVisibility(item.disambiguation, binding.itemDisambiguation)
        setViewVisibility(item.primary, binding.itemPrimary)
        setViewVisibility(item.secondary, binding.itemSecondary)
    }

    private fun setViewVisibility(text: String?, view: TextView) {
        if (text != null && text.isNotEmpty() && !text.equals("null", ignoreCase = true)) {
            view.visibility = View.VISIBLE
            view.text = text
        } else view.visibility = View.GONE
    }

}