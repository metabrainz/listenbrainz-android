package org.listenbrainz.android.service

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

/** Read-only state of [ListenSubmissionService], for classes that only observe it. */
interface ListenServiceState {
    /** True while Android has the listener bound: between onListenerConnected and onListenerDisconnected. */
    val connected: StateFlow<Boolean>
}

/** The writable state, a singleton injected into [ListenSubmissionService], which alone updates it. */
class MutableListenServiceState : ListenServiceState {
    override val connected = MutableStateFlow(false)
}
