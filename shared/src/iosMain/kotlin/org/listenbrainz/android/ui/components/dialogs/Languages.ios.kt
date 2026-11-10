package org.listenbrainz.android.ui.components.dialogs

import platform.Foundation.NSLocale
import platform.Foundation.availableLocaleIdentifiers
import platform.Foundation.currentLocale
import platform.Foundation.languageCode
import platform.Foundation.localizedStringForLanguageCode

actual fun availableLanguages(): List<Language> =
    NSLocale.availableLocaleIdentifiers
        .map { NSLocale(localeIdentifier = it as String).languageCode }
        .distinct()
        .map { it.toLanguage() }

actual fun defaultLanguage(): Language = NSLocale.currentLocale.languageCode.toLanguage()

private fun String.toLanguage() = Language(
    code = this,
    displayName = NSLocale.currentLocale.localizedStringForLanguageCode(this) ?: this
)
