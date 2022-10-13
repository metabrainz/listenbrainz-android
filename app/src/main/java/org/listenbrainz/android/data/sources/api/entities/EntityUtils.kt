package org.listenbrainz.android.data.sources.api.entities

object EntityUtils {
    @JvmStatic
    fun getDisplayArtist(artistCredits: MutableList<ArtistCredit>?): String {
        val builder = StringBuilder()
        val iterator = artistCredits!!.iterator()
        while (iterator.hasNext()) {
            val credit = iterator.next()
            if (credit.name != null &&
                    !credit.name.equals("null", ignoreCase = true)) {
                builder.append(credit.name)
                if (iterator.hasNext()) builder.append(credit.joinphrase)
            }
        }
        return builder.toString()
    }
}