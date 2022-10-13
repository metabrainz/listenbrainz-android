package org.listenbrainz.android.presentation.features.adapters

import androidx.recyclerview.widget.DiffUtil

class ResultItemComparator : DiffUtil.ItemCallback<ResultItem>() {
    override fun areItemsTheSame(oldItem: ResultItem, newItem: ResultItem): Boolean {
        return oldItem === newItem
    }

    override fun areContentsTheSame(oldItem: ResultItem, newItem: ResultItem): Boolean {
        return oldItem.mBID.equals(newItem.mBID, ignoreCase = true)
    }
}