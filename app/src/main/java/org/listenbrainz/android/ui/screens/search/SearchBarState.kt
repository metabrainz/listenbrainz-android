package org.listenbrainz.android.ui.screens.search

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.Saver
import androidx.compose.runtime.saveable.SaverScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue

/** Get state of app's main search bar. If an instance is already defined, that instance will be returned.*/
@Composable
fun rememberSearchBarState(): SearchBarState {
    return SearchBarState.instance ?: rememberSaveable(saver = SearchBarStateSaver()) { SearchBarState() }
}

private class SearchBarStateSaver : Saver<SearchBarState, Boolean> {
    override fun restore(value: Boolean): SearchBarState
        = SearchBarState(value)
    
    override fun SaverScope.save(value: SearchBarState): Boolean {
        value.resetInstance()
        return value.isActive
    }
}

/** State class which controls the main search bar of the app. */
class SearchBarState(initialState: Boolean = false) {
    
    private var state by mutableStateOf(initialState,)
    
    init {
        instance = this
    }
    
    /** True is search bar is active.*/
    val isActive: Boolean
        get() = this.state
    
    /** Show or activate the search bar.*/
    fun activate() { state = true }
    
    /** Hide or deactivate the search bar.*/
    fun deactivate() { state = false }
    
    internal fun resetInstance() { instance = null }
    
    companion object {
        var instance: SearchBarState? = null
            private set
    }
    
}
