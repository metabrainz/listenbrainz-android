package org.listenbrainz.android.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import org.listenbrainz.android.model.ResponseError

abstract class BaseViewModel<UiState>: ViewModel() {
    
    protected val errorFlow = MutableStateFlow<ResponseError?>(null)
    protected val successMsgFlow = MutableStateFlow<Int?>(null)
    /** Visible Ui-State for UI to consume.*/
    abstract val uiState: StateFlow<UiState>
    
    /** Create Ui state's flow in this function using combine and assign to [uiState].*/
    protected abstract fun createUiStateFlow(): StateFlow<UiState>
    
    protected fun emitError(error: ResponseError?) {
        viewModelScope.launch {
            errorFlow.emit(error)
        }
    }

    protected fun emitMsg(messageId : Int?){
        viewModelScope.launch {
            successMsgFlow.emit(messageId)
        }
    }
    
    /** Call this function to reset [errorFlow]'s latest emission.*/
    fun clearErrorFlow() {
        emitError(null)
    }

    fun clearMsgFlow() {
        emitMsg(null)
    }
}