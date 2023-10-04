package org.listenbrainz.android.ui.screens.feed.dialogs

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.Saver
import androidx.compose.runtime.saveable.SaverScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue

/** Get state of dialogs. If an instance is already defined, that instance will be returned.*/
@Composable
fun rememberDialogsState(): DialogsState {
    return rememberSaveable(saver = DialogsState.Companion.DialogsStateSaver()) {
        DialogsState(Dialog.NONE)
    }
}

class DialogsState(
    initialDialog: Dialog,
    initialPage: Int? = null,
    initialEventIndex: Int? = null
) {
    
    /** True if any dialog is active.*/
    var currentDialog: Dialog by mutableStateOf(initialDialog)
        private set
    
    var currentPage: Int? by mutableStateOf(initialPage)
        private set
    
    var currentEventIndex: Int? by mutableStateOf(initialEventIndex)
        private set
    
    fun activateDialog(
        dialog: Dialog,
        page: Int,
        eventIndex: Int
    ) {
        // Activation order is important!
        currentEventIndex = eventIndex
        currentPage = page
        currentDialog = dialog
    }
    
    fun deactivateDialog() {
        // Deactivation order is important!
        currentDialog = Dialog.NONE
        currentPage = null
        currentEventIndex = null
    }
    
    companion object {
        class DialogsStateSaver : Saver<DialogsState, Triple<Int, Int?, Int?>> {
            override fun restore(value: Triple<Int, Int?, Int?>): DialogsState
                    = DialogsState(Dialog.getDialogFromOrdinal(value.first), value.second, value.third)
        
            override fun SaverScope.save(value: DialogsState): Triple<Int, Int?, Int?> {
                return Triple(value.currentDialog.ordinal, value.currentPage, value.currentEventIndex)
            }
        }
    }
}

enum class Dialog {
    NONE,
    PIN,
    PERSONAL_RECOMMENDATION,
    REVIEW;
    
    companion object {
        fun getDialogFromOrdinal(ordinal: Int): Dialog =
            when (ordinal) {
                NONE.ordinal -> NONE
                PIN.ordinal -> PIN
                PERSONAL_RECOMMENDATION.ordinal -> PERSONAL_RECOMMENDATION
                REVIEW.ordinal -> REVIEW
                else -> NONE
            }
    }
}
