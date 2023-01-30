package org.listenbrainz.sharedtest.utils

import org.hamcrest.MatcherAssert.assertThat
import org.junit.Assert.assertEquals
import org.hamcrest.collection.IsIterableContainingInAnyOrder.containsInAnyOrder
import org.listenbrainz.android.data.sources.api.entities.WikiSummary
import org.listenbrainz.android.data.sources.api.entities.mbentity.Collection
import org.listenbrainz.android.data.sources.api.entities.mbentity.*
import org.listenbrainz.android.presentation.features.adapters.ResultItem

object AssertionUtils {
    fun checkWikiAssertions(testSummary: WikiSummary, summary: WikiSummary) {
        assertEquals(testSummary.extract, summary.extract)
    }

    fun checkReleaseAssertions(testRelease: Release, release: Release) {
        assertEquals(testRelease, release)
        assertEquals(testRelease.title, release.title)
        assertEquals(testRelease.barcode, release.barcode)
        assertEquals(testRelease.status, release.status)
        assertEquals(testRelease.country, release.country)
        assertEquals(testRelease.disambiguation, release.disambiguation)
        assertEquals(testRelease.date, release.date)
        if (testRelease.artistCredits.size != 0){
            assertThat(testRelease.artistCredits, containsInAnyOrder(release.artistCredits.toTypedArray()))
        }
    }

    fun checkArtistAssertions(testArtist: Artist, artist: Artist) {
        assertEquals(testArtist, artist)
        assertEquals(testArtist.country, artist.country)
        assertEquals(testArtist.disambiguation, artist.disambiguation)
        assertEquals(testArtist.name, artist.name)
        assertEquals(testArtist.sortName, artist.sortName)
        assertEquals(testArtist.gender, artist.gender)
        assertEquals(testArtist.type, artist.type)
        //Need to fix this
        //if (testArtist.getReleases().isNotEmpty()) {
        //  assertThat(testArtist.getReleases(), containsInAnyOrder(artist.getReleases().toTypedArray()))
        //}
    }

    fun checkReleaseGroupAssertions(testReleaseGroup: ReleaseGroup, releaseGroup: ReleaseGroup) {
        assertEquals(testReleaseGroup, releaseGroup)
        assertEquals(testReleaseGroup.title, releaseGroup.title)
        assertEquals(testReleaseGroup.count, releaseGroup.count)
        assertEquals(testReleaseGroup.primaryType, releaseGroup.primaryType)
        assertEquals(testReleaseGroup.fullType, releaseGroup.fullType)
        assertEquals(testReleaseGroup.disambiguation, releaseGroup.disambiguation)
        if (testReleaseGroup.artistCredits.size != 0) {
            assertThat(testReleaseGroup.artistCredits, containsInAnyOrder(releaseGroup.artistCredits.toTypedArray()))
        }
    }

    fun checkLabelAssertions(testLabel: Label, label: Label) {
        assertEquals(testLabel, label)
        assertEquals(testLabel.code, label.code)
        assertEquals(testLabel.type, label.type)
        assertEquals(testLabel.country, label.country)
    }

    fun checkRecordingAssertions(testRecording: Recording, recording: Recording) {
        assertEquals(testRecording, recording)
        assertEquals(testRecording.title, testRecording.title)
        assertEquals(testRecording.duration, recording.duration)
        assertEquals(testRecording.length, recording.length)
        assertEquals(testRecording.trackCount, recording.trackCount)
        assertEquals(testRecording.disambiguation, recording.disambiguation)
        if (testRecording.artistCredits.size != 0) {
            assertThat(testRecording.artistCredits, containsInAnyOrder(recording.artistCredits.toTypedArray()))
        }
    }

    fun checkResultItemAssertions(testItem: ResultItem, item: ResultItem) {
        assertEquals(testItem.mBID, item.mBID)
        assertEquals(testItem.disambiguation, item.disambiguation)
        assertEquals(testItem.name, item.name)
        assertEquals(testItem.primary, item.primary)
        assertEquals(testItem.secondary, item.secondary)
    }

    fun checkCollectionDetailsAssertions(testCollectionDetails: ResultItem, collectionDetails: ResultItem) {
        assertEquals(testCollectionDetails.name, collectionDetails.name)
        assertEquals(testCollectionDetails.mBID, collectionDetails.mBID)
    }

    fun checkCollectionAssertions(testCollection: Collection, collection: Collection) {
        assertEquals(testCollection.mbid, collection.mbid)
        assertEquals(testCollection.type, collection.type)
        assertEquals(testCollection.name, collection.name)
        assertEquals(testCollection.count, collection.count)
        assertEquals(testCollection.editor, collection.editor)
        assertEquals(testCollection.entityType, collection.entityType)
    }
}