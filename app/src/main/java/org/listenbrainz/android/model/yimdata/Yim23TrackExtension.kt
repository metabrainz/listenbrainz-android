package org.listenbrainz.android.model.yimdata

import com.google.gson.annotations.SerializedName

data class Yim23TrackExtension (
    @SerializedName("https://musicbrainz.org/doc/jspf#track") var extensionData    : Yim23TrackExtensionData   = Yim23TrackExtensionData(),

)
