package org.listenbrainz.android

import android.content.Context
import org.junit.Assert.assertEquals
import org.junit.Test
import org.listenbrainz.android.EntityTestUtils.testYimData
import org.listenbrainz.android.presentation.features.yim.YearInMusicActivity
import org.listenbrainz.android.presentation.features.yim.YimViewModel
import org.mockito.Mockito.mock

class YimViewModelTest{
    
    @Test
    fun yimViewModelTest() {
        val expected = testYimData
        // FIXME : Do i need to test this too?
        //val viewModel = YimViewModel (MockYimRepository(), mock(Context::class.java))
        //assertEquals(viewModel.getUserName(), expected.payload.userName)
    }
}