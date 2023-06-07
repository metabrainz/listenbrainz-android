package org.listenbrainz.android

import org.junit.Assert.assertEquals
import org.junit.Test
import org.listenbrainz.android.util.LinkedService

class LinkedServiceUtilTest {
    
    @Test
    fun `parse service test`(){
        var service = "spotify"
        assertEquals(LinkedService.SPOTIFY, LinkedService.parseService(service))
        
        service = "musicbrainz"
        assertEquals(LinkedService.MUSICBRAINZ, LinkedService.parseService(service))
        
        service = "critiquebrainz"
        assertEquals(LinkedService.CRITIQUEBRAINZ, LinkedService.parseService(service))
        
        service = "apple_music"
        assertEquals(LinkedService.UNKNOWN, LinkedService.parseService(service))
    }
}