package org.listenbrainz.sharedtest.utils

import org.listenbrainz.android.presentation.features.adapters.ResultItem
import org.listenbrainz.sharedtest.utils.EntityTestUtils.testArtistMBID
import org.listenbrainz.sharedtest.utils.EntityTestUtils.testLabelMBID
import org.listenbrainz.sharedtest.utils.EntityTestUtils.testRecordingMBID
import org.listenbrainz.sharedtest.utils.EntityTestUtils.testReleaseGroupMBID
import org.listenbrainz.sharedtest.utils.EntityTestUtils.testReleaseMBID

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