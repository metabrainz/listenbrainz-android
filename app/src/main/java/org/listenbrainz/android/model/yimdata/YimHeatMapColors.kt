package org.listenbrainz.android.model.yimdata

import androidx.compose.ui.graphics.Color

data class YimHeatMapColors (
    val greaterThan150 : Color,
    var greaterThan100 : Color,
    val greaterThan50: Color,
    val greaterThan0 : Color
)