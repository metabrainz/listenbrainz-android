package org.listenbrainz.android.model

enum class UiMode {
    FOLLOW_SYSTEM,
    LIGHT,
    DARK;
    
    companion object {
        fun String?.asUiMode(): UiMode =
            when (this) {
                DARK.name -> DARK
                LIGHT.name -> LIGHT
                else -> FOLLOW_SYSTEM
            }
    }
}