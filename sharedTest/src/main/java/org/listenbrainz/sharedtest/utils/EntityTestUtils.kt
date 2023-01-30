package org.listenbrainz.sharedtest.utils

import org.junit.Assert.fail
import org.listenbrainz.android.data.sources.api.entities.ArtistCredit
import org.listenbrainz.android.data.sources.api.entities.WikiSummary
import org.listenbrainz.android.data.sources.api.entities.mbentity.Collection
import org.listenbrainz.android.data.sources.api.entities.mbentity.*
import org.listenbrainz.android.presentation.features.adapters.ResultItem
import java.io.BufferedReader
import java.io.IOException
import java.io.InputStreamReader
import java.util.*

object EntityTestUtils {

    fun loadResourceAsString(resource: String?): String {
        val builder = StringBuilder()
        try {
            ClassLoader.getSystemClassLoader().getResourceAsStream(resource).use { inputStream ->
                BufferedReader(
                    InputStreamReader(inputStream)
                ).use { reader ->
                    var line: String?
                    while (reader.readLine().also { line = it } != null) builder.append(line)
                }
            }
        } catch (e: IOException) {
            e.printStackTrace()
            fail()
        }
        return builder.toString()
    }

    val testArtistMBID: String
        get() = "b8a7c51f-362c-4dcb-a259-bc6e0095f0a6"

    val testArtist: Artist
        get() {
            val MBID = testArtistMBID
            val testArtist = Artist()
            testArtist.mbid = MBID
            testArtist.type = "Person"
            testArtist.country = "GB"
            testArtist.disambiguation = "UK singer-songwriter"
            testArtist.name = "Ed Sheeran"
            testArtist.sortName = "Sheeran, Ed"
            testArtist.gender = "Male"
            val testReleases: MutableList<Release> = ArrayList()
            var testRelease = Release()
            testRelease.mbid = "8b6d9a7e-2f94-4cca-b2e3-d35116b1a49c"
            testRelease.date = "2005"
            testRelease.disambiguation = ""
            testRelease.title = "The Orange Room EP"
            testRelease.status = "Official"
            testRelease.country = "XW"
            testRelease.barcode = "8791423234"
            testReleases.add(testRelease)
            testRelease = Release()
            testRelease.mbid = "71c07e92-4b71-43d3-a041-5fd67d8343af"
            testRelease.date = "2006-03-22"
            testRelease.disambiguation = ""
            testRelease.title = "Ed Sheeran"
            testRelease.status = "Official"
            testRelease.country = "GB"
            testRelease.barcode = "12223213211"
            testReleases.add(testRelease)
            testRelease = Release()
            testRelease.mbid = "fe2c71cf-52e3-4abd-a0ed-45ab3770d965"
            testRelease.date = "2006-03-22"
            testRelease.disambiguation = ""
            testRelease.title = "Ed Sheeran (EP)"
            testRelease.status = "Official"
            testRelease.country = "GB"
            testRelease.barcode = "1221421"
            testReleases.add(testRelease)
            testRelease = Release()
            testRelease.mbid = "3c8473d3-202c-44c8-a37b-8e259fe12c39"
            testRelease.date = "2007-05-01"
            testRelease.disambiguation = ""
            testRelease.title = "Want Some?"
            testRelease.status = "Official"
            testRelease.country = "GB"
            testRelease.barcode = "8791423234"
            testReleases.add(testRelease)
            testArtist.setReleases(testReleases)
            return testArtist
        }

    val testReleaseMBID: String
        get() = "94ad3a58-a1cc-46a3-acf4-9cb6c1d6f032"

    val testRelease: Release
        get() {
            val MBID = testReleaseMBID
            val testRelease = Release()
            testRelease.mbid = MBID
            testRelease.title = "+"
            testRelease.barcode = "5052498646524"
            testRelease.status = "Official"
            testRelease.country = "XE"
            testRelease.disambiguation = ""
            testRelease.date = "2011-09-09"
            val testArtistCredits: ArrayList<ArtistCredit> = ArrayList<ArtistCredit>()
            val testArtistCredit = ArtistCredit()
            testArtistCredit.joinphrase = ""
            testArtistCredit.name = "Ed Sheeran"
            val testArtist = Artist()
            testArtist.mbid = "b8a7c51f-362c-4dcb-a259-bc6e0095f0a6"
            testArtistCredit.artist = testArtist
            testRelease.artistCredits = testArtistCredits
            return testRelease
        }

    val testReleaseGroupMBID: String
        get() = "05ce100c-eddf-4967-8d7e-33fc0883fe39"

    val testReleaseGroup: ReleaseGroup
        get() {
            val MBID = testReleaseGroupMBID
            val testReleaseGroup = ReleaseGroup()
            testReleaseGroup.mbid = MBID
            testReleaseGroup.count = 1
            testReleaseGroup.title = "+"
            testReleaseGroup.primaryType = "Album"
            testReleaseGroup.disambiguation = "plus"
            val testArtistCredits: List<ArtistCredit> = ArrayList<ArtistCredit>()
            val testArtistCredit = ArtistCredit()
            testArtistCredit.joinphrase = ""
            testArtistCredit.name = "Ed Sheeran"
            val testArtist = Artist()
            testArtist.mbid = "b8a7c51f-362c-4dcb-a259-bc6e0095f0a6"
            testArtistCredit.artist = testArtist
            testReleaseGroup.setArtistCredits(testArtistCredits)
            return testReleaseGroup
        }

    val testLabelMBID: String
        get() = "015a28ab-1eb8-45a0-b2c6-601e410548af"

    val testLabel: Label
        get() {
            val MBID = testLabelMBID
            val testLabel = Label()
            testLabel.mbid = MBID
            testLabel.name = "Speed Records"
            testLabel.code = "SPREC"
            testLabel.country = "IN"
            testLabel.disambiguation = "India"
            testLabel.type = "Original Production"
            return testLabel
        }

    val testRecordingMBID: String
        get() = "11f3c37d-839f-4f65-87c1-d55cb416d0c5"

    val testRecording: Recording
        get() {
            val MBID = testRecordingMBID
            val testRecording = Recording()
            testRecording.mbid = MBID
            testRecording.length = 125400
            testRecording.title = "Plus Plus"
            testRecording.disambiguation = "no disambiguation"
            val testArtistCredits: ArrayList<ArtistCredit> = ArrayList<ArtistCredit>()
            val testArtistCredit = ArtistCredit()
            testArtistCredit.joinphrase = ""
            testArtistCredit.name = "Like a Tim"
            val testArtist = Artist()
            testArtist.mbid = "90568a1e-ef8d-4d22-93fa-faadc819d576"
            testArtistCredit.artist = testArtist
            testRecording.artistCredits = testArtistCredits
            return testRecording
        }
}