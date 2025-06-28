package org.listenbrainz.android.ui.screens.onboarding.introduction

import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle
import org.listenbrainz.android.R
import org.listenbrainz.android.ui.theme.ListenBrainzTheme

enum class IntroScreenDataEnum(val title: String,
                               val subtitle: String,
                               val highlight: String?,
                               val screenNo: Int,
                               val res: Int) {
    LISTENBRAINZ(
        title = "Listen together with Listenbrainz",
        subtitle = "Track, explore, visualise and share the music you listen to. Follow your favourites and discover great music.",
        highlight = null,
        screenNo = 1,
        res = R.drawable.intro_headphones
    ),
    BRAINZPLAYER(
        title = "Enjoy your local music with BrainzPlayer",
        highlight = null,
        subtitle = "Play songs from your device effortlessly. Experience a smooth, organized, and enriched music playback experience.",
        screenNo = 2,
        res = R.drawable.intro_speakers
    ),
    BUGS(
        title = "Facing any Issue? \nSubmit Logs!",
        highlight = "Settings > Report an issue",
        subtitle = "If you face any issues, you can submit logs to help us troubleshoot. Go to ",
        screenNo = 3,
        res = R.drawable.intro_bug_report
    );

    companion object{
        fun getScreenData(screenNo: Int): IntroScreenDataEnum {
            return entries.first { it.screenNo == screenNo }
        }
    }


    val isLast: Boolean
    get() = screenNo == IntroScreenDataEnum.entries.size
}