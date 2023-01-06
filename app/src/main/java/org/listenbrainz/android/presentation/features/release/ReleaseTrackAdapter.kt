package org.listenbrainz.android.presentation.features.release

import android.content.Context
import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import org.listenbrainz.android.data.sources.Constants
import org.listenbrainz.android.data.sources.api.entities.EntityUtils
import org.listenbrainz.android.data.sources.api.entities.Media
import org.listenbrainz.android.data.sources.api.entities.Track
import org.listenbrainz.android.databinding.ItemTrackBinding
import org.listenbrainz.android.databinding.ItemTrackHeadingBinding
import org.listenbrainz.android.presentation.features.recording.RecordingActivity

class ReleaseTrackAdapter(private val mediaList: List<Media>?) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {
    override fun getItemViewType(position: Int): Int {
        /*There are two view types to be displayed in the RecyclerView.
         * 1) The titles of mediums in which the tracks are present.
         * 2) The tracks itself.
         * To find which view type, the following method is used:
         * Check if the position is zero or equal to previous cumulative trackLis count. This would mean
         * the position id for the title of a medium. Repeat this till all mediums are accounted for and
         * return the view type as a trackLis.
         */
        var calculate = 0
        var flag = VIEWTYPE_TRACK
        for (media in mediaList!!) {
            if (calculate == position) {
                flag = VIEWTYPE_HEADING
                break
            }
            val trackCount = media.trackCount
            calculate += trackCount
            ++calculate
        }
        return flag
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val layoutInflater = parent.context
                .getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
        return if (viewType == VIEWTYPE_TRACK)
            TrackViewHolder(ItemTrackBinding.inflate(layoutInflater, parent, false))
        else
            TrackHeadingViewHolder(ItemTrackHeadingBinding.inflate(layoutInflater, parent, false))
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        if (getItemViewType(position) == 0)
            (holder as TrackViewHolder).bind(getTrack(position))
        else
            (holder as TrackHeadingViewHolder).bind(getMediumTitle(position))
    }

    override fun getItemCount(): Int {
        /*
         * Total count of items in recycler view is equal to cumulative trackLis count of the media and
         * number of media because the titles of media of also make up one item each.*/
        var count = 0
        if (mediaList != null) {
            for (medium in mediaList) count += medium.trackCount
            count += mediaList.size
        }
        return count
    }

    private fun setViewVisibility(text: String?, view: TextView) {
        if (text != null && !text.isEmpty() && !text.equals("null", ignoreCase = true)) {
            view.visibility = View.VISIBLE
            view.text = text
        } else view.visibility = View.GONE
    }

    private fun getMediumTitle(position: Int): String {
        /*
         * Deduct the trackLis count of each media from the cumulative count. Reduce one from the count
         * for the already displayed medium's title. When the count becomes zero, the next media is
         * the one whose title has to be displayed.
         * */
        var pos = position
        for (media in mediaList!!) {
            if (pos == 0) {
                var format = media.format
                if (format == null || format.equals("null", ignoreCase = true)) format = "Medium"
                return format + "  " + media.position + " : " + media.title
            }
            pos -= media.trackCount
            --pos
        }
        return ""
    }

    private fun getTrack(position: Int): Track {
        /*
         * To calculate trackLis position, deduct the trackLis count of all previous media that is till the
         * trackLis count is positive. This way we get the trackLis position in the required media and
         * then return it.*/
        var pos = position
        for (media in mediaList!!) {
            --pos
            if (pos < media.trackCount) return media.tracks[pos]
            pos -= media.trackCount
        }
        return Track()
    }

    inner class TrackViewHolder(var binding: ItemTrackBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(item: Track) {
            setViewVisibility(item.title, binding.trackName)
            setViewVisibility(item.position.toString(), binding.trackNumber)
            setViewVisibility(item.duration, binding.trackTime)
            setViewVisibility(EntityUtils.getDisplayArtist(item.recording!!.artistCredits),
                    binding.trackArtist)
            itemView.setOnClickListener { v: View ->
                val intent = Intent(v.context, RecordingActivity::class.java)
                intent.putExtra(Constants.MBID, item.recording!!.mbid)
                v.context.startActivity(intent)
            }
        }
    }

    inner class TrackHeadingViewHolder(var binding: ItemTrackHeadingBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(title: String?) {
            setViewVisibility(title, binding.mediumTitle)
        }
    }

    companion object {
        private const val VIEWTYPE_TRACK = 0
        private const val VIEWTYPE_HEADING = 1
    }
}