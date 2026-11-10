package org.listenbrainz.android.ui.screens.onboarding.introduction

import org.jetbrains.compose.resources.DrawableResource
import org.listenbrainz.shared.resources.Res
import org.listenbrainz.shared.resources.intro_bug_report
import org.listenbrainz.shared.resources.intro_headphones

enum class IntroScreenDataEnum(
    val title: String,
    val subtitle: String,
    val highlight: String?,
    val screenNo: Int,
    val res: DrawableResource
) {
    LISTENBRAINZ(
        title = "Listen together with Listenbrainz",
        subtitle = "Track, explore, visualise and share the music you listen to. Follow your favourites and discover great music.",
        highlight = null,
        screenNo = 1,
        res = Res.drawable.intro_headphones
    ),
    BUGS(
        title = "Facing any Issue? \nSubmit Logs!",
        highlight = "Settings > Report an issue",
        subtitle = "If you face any issues, you can submit logs to help us troubleshoot. Go to ",
        screenNo = 2,
        res = Res.drawable.intro_bug_report
    );

    companion object {
        fun getScreenData(screenNo: Int): IntroScreenDataEnum {
            return entries.first { it.screenNo == screenNo }
        }
    }


    val isLast: Boolean
        get() = screenNo == IntroScreenDataEnum.entries.size
}