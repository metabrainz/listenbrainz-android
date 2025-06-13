package org.listenbrainz.android.ui.screens.onboarding.introduction

import org.listenbrainz.android.R

enum class IntroScreenDataEnum(val title: String, val subtitle: String, val screenNo: Int, val res: Int) {
    LISTENBRAINZ(
        title = "Listen together with Listenbrainz",
        subtitle = "Track, explore, visualise and share the music you listen to. Follow your favourites and discover great music.",
        screenNo = 1,
        res = R.drawable.intro_headphones
    ),
    BRAINZPLAYER(
        title = "Enjoy your local music with BrainzPlayer",
        subtitle = "Play songs from your device effortlessly. Experience a smooth, organized, and enriched music playback experience.",
        screenNo = 2,
        res = R.drawable.intro_speakers
    );

    companion object{
        fun getScreenData(screenNo: Int): IntroScreenDataEnum {
            return entries.first { it.screenNo == screenNo }
        }
    }


    val isLast: Boolean
    get() = screenNo == IntroScreenDataEnum.entries.size
}