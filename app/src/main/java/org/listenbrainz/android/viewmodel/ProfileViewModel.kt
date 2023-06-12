package org.listenbrainz.android.viewmodel

import android.content.Context
import android.widget.Toast
import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.flow
import org.listenbrainz.android.repository.preferences.AppPreferences
import javax.inject.Inject

@HiltViewModel
class ProfileViewModel @Inject constructor(val appPreferences: AppPreferences) : ViewModel() {
    
    fun getLoginStatusFlow(): Flow<Int> {
        return flow {
            while (true){
                delay(200)
                emit(appPreferences.loginStatus)
            }
        }.distinctUntilChanged()
    }

    
    
    fun logoutUser(context: Context) {
        appPreferences.logoutUser()
        Toast.makeText(context, "User has successfully logged out.", Toast.LENGTH_SHORT).show()
    }
}