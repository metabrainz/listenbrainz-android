package org.listenbrainz.android.util

import android.content.Context
import android.database.Cursor
import android.net.Uri
import android.widget.FilterQueryProvider
import androidx.cursoradapter.widget.SimpleCursorAdapter
import androidx.loader.content.CursorLoader
import org.listenbrainz.android.R

/**
 * This is a helper class to provide a Cursor to the suggestions database.
 */
class SuggestionHelper(private val context: Context) {
    val adapter: SimpleCursorAdapter
        get() {
            val adapter = SimpleCursorAdapter(context, R.layout.dropdown_item, null, FROM, TO)
            adapter.cursorToStringConverter = SuggestionCursorToString()
            adapter.filterQueryProvider = SuggestionFilterQuery()
            return adapter
        }

    fun getMatchingEntries(constraint: String?): Cursor? {
        if (constraint == null) {
            return null
        }
        val where = "$COLUMN LIKE ?"
        val args = arrayOf(constraint.trim { it <= ' ' } + "%")
        val cursorLoader = CursorLoader(context, Uri.parse(URI), null, where, args, "$COLUMN ASC")
        val cursor = cursorLoader.loadInBackground()
        cursor?.moveToFirst()
        return cursor
    }

    private class SuggestionCursorToString : SimpleCursorAdapter.CursorToStringConverter {
        override fun convertToString(cursor: Cursor): CharSequence {
            val columnIndex = cursor.getColumnIndexOrThrow(COLUMN)
            return cursor.getString(columnIndex)
        }
    }

    private inner class SuggestionFilterQuery : FilterQueryProvider {
        override fun runQuery(constraint: CharSequence): Cursor? {
            return getMatchingEntries(constraint.toString())
        }
    }

    companion object {
        private const val COLUMN = "display1"
        private const val URI = "content://" + SuggestionProvider.AUTHORITY + "/suggestions"
        private val FROM = arrayOf(COLUMN)
        private val TO = intArrayOf(R.id.dropdown_item)
    }
}