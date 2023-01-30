package org.listenbrainz.android

import com.google.gson.Gson
import org.junit.Test
import org.listenbrainz.sharedtest.utils.EntityTestUtils.loadResourceAsString
import org.listenbrainz.sharedtest.utils.EntityTestUtils.testArtist
import org.listenbrainz.sharedtest.utils.EntityTestUtils.testLabel
import org.listenbrainz.sharedtest.utils.EntityTestUtils.testRecording
import org.listenbrainz.sharedtest.utils.EntityTestUtils.testRelease
import org.listenbrainz.sharedtest.utils.EntityTestUtils.testReleaseGroup
import org.listenbrainz.android.data.sources.api.entities.mbentity.*
import org.listenbrainz.sharedtest.utils.AssertionUtils

class EntityModelsTest {
    @Test
    fun testArtistModel() {
        val artist: Artist = Gson().fromJson(loadResourceAsString("artist_lookup.json"),Artist::class.java)
        AssertionUtils.checkArtistAssertions(testArtist, artist)
    }

    @Test
    fun testReleaseModel() {
        val release: Release = Gson().fromJson(
            loadResourceAsString("release_lookup.json"),
            Release::class.java)
        AssertionUtils.checkReleaseAssertions(testRelease, release)
    }

    @Test
    fun testReleaseGroupModel() {
        val releaseGroup: ReleaseGroup = Gson().fromJson(
            loadResourceAsString("release-group_lookup.json"),
            ReleaseGroup::class.java
        )
        AssertionUtils.checkReleaseGroupAssertions(testReleaseGroup, releaseGroup)
    }

    @Test
    fun testLabelModel() {
        val label: Label = Gson().fromJson(
            loadResourceAsString("label_lookup.json"),
            Label::class.java)
        AssertionUtils.checkLabelAssertions(testLabel, label)
    }

    @Test
    fun testRecordingModel() {
        val recording: Recording = Gson().fromJson(
            loadResourceAsString("recording_lookup.json"),
            Recording::class.java)
        AssertionUtils.checkRecordingAssertions(testRecording, recording)
    }
}