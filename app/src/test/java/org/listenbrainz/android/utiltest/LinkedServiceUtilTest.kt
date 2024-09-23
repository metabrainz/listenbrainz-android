package org.listenbrainz.android.utiltest

import org.junit.Assert.assertEquals
import org.junit.Test
import org.listenbrainz.android.util.LinkedService
import org.listenbrainz.android.util.LinkedService.Companion.toLinkedService

class LinkedServiceUtilTest {
    
    @Test
    fun `parse service test`(){
        var service = "spotify"
        assertEquals(LinkedService.SPOTIFY, service.toLinkedService())
        
        service = "musicbrainz"
        assertEquals(LinkedService.MUSICBRAINZ, service.toLinkedService())
        
        service = "critiquebrainz"
        assertEquals(LinkedService.CRITIQUEBRAINZ, service.toLinkedService())
        
        service = "apple_music"
        assertEquals(LinkedService.UNKNOWN, service.toLinkedService())
    }
}