package org.listenbrainz.android.model.yimdata

import androidx.annotation.DrawableRes
import org.listenbrainz.android.R

enum class Yim23ThemeData(@DrawableRes val pickColorRes: Int , @DrawableRes val homeIllustration: Int , @DrawableRes val heartRes: Int) {
    GREEN(pickColorRes = R.drawable.yim23_pick_color_green , homeIllustration = R.drawable.yim23_2023_green , heartRes = R.drawable.yim23_g_heart),
    RED(pickColorRes = R.drawable.yim23_pick_color_red , homeIllustration = R.drawable.yim23_2023_red , heartRes = R.drawable.yim23_red_heart),
    BLUE(pickColorRes = R.drawable.yim23_pick_color_blue , homeIllustration = R.drawable.yim23_2023_blue , heartRes = R.drawable.yim23_blue_heart),
    GRAY(pickColorRes = R.drawable.yim23_pick_color_grey , homeIllustration = R.drawable.yim23_2023_grey , heartRes = R.drawable.yim23_grey_heart)
}