package org.listenbrainz.android.ui.screens.feed.dialogs

import android.os.Bundle
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
    
    var metadata: Bundle? by mutableStateOf(null)
        private set
    
    fun activateDialog(
        dialog: Dialog,
        metadata: Bundle,
    ) {
        // Activation order is important!
        this.metadata = metadata
        currentDialog = dialog
    }
    
    fun deactivateDialog() {
        // Deactivation order is important!
        currentDialog = Dialog.NONE
        metadata = null
    }
    
    companion object {
        class DialogsStateSaver : Saver<DialogsState, Int> {
            override fun restore(value: Int): DialogsState
                    = DialogsState(Dialog.getDialogFromOrdinal(value))
        
            override fun SaverScope.save(value: DialogsState): Int {
                return value.currentDialog.ordinal
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
