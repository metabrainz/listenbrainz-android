package org.listenbrainz.android.util

import androidx.compose.runtime.saveable.Saver
import androidx.compose.runtime.saveable.SaverScope
import androidx.compose.runtime.snapshots.SnapshotStateList

class StringSnapshotStateListSaver : Saver<SnapshotStateList<String>, Array<String>> {
    override fun restore(value: Array<String>): SnapshotStateList<String> {
        return SnapshotStateList<String>().apply {
            addAll(value)
        }
    }
    
    override fun SaverScope.save(value: SnapshotStateList<String>): Array<String> {
        return value.toTypedArray()
    }
}