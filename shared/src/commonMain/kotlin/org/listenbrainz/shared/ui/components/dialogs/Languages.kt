package org.listenbrainz.shared.ui.components.dialogs

/** A language by its ISO 639 [code] and its name in the device language. */
data class Language(val code: String, val displayName: String)

expect fun availableLanguages(): List<Language>

expect fun defaultLanguage(): Language
