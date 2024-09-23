package org.listenbrainz.android.ui.components.dialogs

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
    initialMetadata: Bundle? = null
) {
    
    /** True if any dialog is active.*/
    var currentDialog: Dialog by mutableStateOf(initialDialog)
        private set
    
    var metadata: Bundle? by mutableStateOf(initialMetadata)
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
        class DialogsStateSaver : Saver<DialogsState, Pair<Int, Bundle?>> {
            override fun restore(value: Pair<Int, Bundle?>): DialogsState
                    = DialogsState(Dialog.getDialogFromOrdinal(value.first), value.second)
        
            override fun SaverScope.save(value: DialogsState): Pair<Int, Bundle?> {
                return Pair(value.currentDialog.ordinal, value.metadata)
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
