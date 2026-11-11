package org.listenbrainz.shared.ui.components.dialogs

import java.util.Locale

actual fun availableLanguages(): List<Language> =
    Locale.getAvailableLocales().map { it.toLanguage() }

actual fun defaultLanguage(): Language = Locale.getDefault().toLanguage()

private fun Locale.toLanguage() = Language(code = language, displayName = displayLanguage)
