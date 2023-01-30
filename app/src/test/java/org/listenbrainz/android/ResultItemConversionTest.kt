package org.listenbrainz.android

import org.listenbrainz.android.presentation.features.adapters.ResultItemUtils.getEntityAsResultItem
import org.junit.Test
import org.listenbrainz.sharedtest.utils.EntityTestUtils.testArtist
import org.listenbrainz.sharedtest.utils.EntityTestUtils.testLabel
import org.listenbrainz.sharedtest.utils.EntityTestUtils.testRecording
import org.listenbrainz.sharedtest.utils.EntityTestUtils.testRelease
import org.listenbrainz.sharedtest.utils.EntityTestUtils.testReleaseGroup
import org.listenbrainz.sharedtest.utils.AssertionUtils
import org.listenbrainz.sharedtest.utils.ResultItemTestUtils

class ResultItemConversionTest {
    @Test
    fun testConvertArtist() {
        val testItem = ResultItemTestUtils.testArtistResultItem
        val item = getEntityAsResultItem(testArtist)
        AssertionUtils.checkResultItemAssertions(testItem, item)
    }

    @Test
    fun testConvertRelease() {
        val testItem = ResultItemTestUtils.testReleaseResultItem
        val item = getEntityAsResultItem(testRelease)
        AssertionUtils.checkResultItemAssertions(testItem, item)
    }

    @Test
    fun testConvertLabel() {
        val testItem = ResultItemTestUtils.testLabelResultItem
        val item = getEntityAsResultItem(testLabel)
        AssertionUtils.checkResultItemAssertions(testItem, item)
    }

    @Test
    fun testConvertRecording() {
        val testItem = ResultItemTestUtils.testRecordingResultItem
        val item = getEntityAsResultItem(testRecording)
        AssertionUtils.checkResultItemAssertions(testItem, item)
    }

    @Test
    fun testConvertReleaseGroup() {
        val testItem = ResultItemTestUtils.testReleaseGroupResultItem
        val item = getEntityAsResultItem(testReleaseGroup)
        AssertionUtils.checkResultItemAssertions(testItem, item)
    }
}