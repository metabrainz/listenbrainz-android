package org.listenbrainz.android

import org.listenbrainz.android.presentation.features.adapters.ResultItem
import org.listenbrainz.android.EntityTestUtils.testArtistMBID
import org.listenbrainz.android.EntityTestUtils.testLabelMBID
import org.listenbrainz.android.EntityTestUtils.testRecordingMBID
import org.listenbrainz.android.EntityTestUtils.testReleaseGroupMBID
import org.listenbrainz.android.EntityTestUtils.testReleaseMBID

object ResultItemTestUtils {
    val testArtistResultItem: ResultItem
        get() = ResultItem(
            testArtistMBID,
            "Ed Sheeran",
            "UK singer-songwriter",
            "Person",
            "GB"
        )
    val testReleaseResultItem: ResultItem
        get() = ResultItem(
            testReleaseMBID,
            "+",
            "",
            "",
            ""
        )
    val testLabelResultItem: ResultItem
        get() = ResultItem(
            testLabelMBID,
            "Speed Records",
            "India",
            "Original Production",
            "IN"
        )
    val testReleaseGroupResultItem: ResultItem
        get() = ResultItem(
            testReleaseGroupMBID,
            "+",
            "plus",
            "",
            "Album"
        )
    val testRecordingResultItem: ResultItem
        get() = ResultItem(
            testRecordingMBID,
            "Plus Plus",
            "no disambiguation",
            "",
            ""
        )
}