package org.listenbrainz.android.ui.screens.search

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue

/** Get state of app's main search bar.*/
@Composable
fun rememberSearchBarState(): SearchBarState {
    return SearchBarState()
}

/** State class which controls the main search bar of the app.*/
class SearchBarState(initialState: Boolean = false) {
    
    private var state by mutableStateOf(initialState)
    
    /** True is search bar is active.*/
    val isActive: Boolean
        get() = this.state
    
    /** Show or activate the search bar.*/
    fun activate() {
        if (!state){
            state = true
        }
    }
    
    /** Hide or deactivate the search bar.*/
    fun deactivate() {
        state = !state
        if (state){
            state = false
        }
    }
    
}
