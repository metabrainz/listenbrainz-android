package org.listenbrainz.sharedtest.testdata

import org.listenbrainz.android.model.playlist.PlaylistData
import org.listenbrainz.android.model.playlist.PlaylistPayload

object PlaylistDataRepositoryTestData {
    val playlistDetailsTestData: PlaylistPayload = PlaylistPayload(
        playlist = PlaylistData(
            annotation = "Description of the playlist",
            title = "Weekly playlist",
            date= "2025-01-12T11:05:24.966018+00:00",
            creator = "hemang-mishra"
        )
    )

    val samplePlaylistCoverArt = """
        <svg version="1.1"
             xmlns="http://www.w3.org/2000/svg"
             xmlns:xlink="http://www.w3.org/1999/xlink"
             role="img"
             viewBox="0 0 500 500"
             width="500"
             height="500">

             
                  <title>Copy of Weekly Exploration for hemang-mishra, week of 2025-02-17 Mon</title>
                  <desc>&lt;p&gt;The ListenBrainz Weekly Exploration playlist helps you discover new music! 
            It may require active listening and skips. The playlist features tracks you haven&#39;t heard before, 
            selected by a collaborative filtering algorithm.&lt;/p&gt;

            &lt;p&gt;Updated every Monday morning based on your timezone.&lt;/p&gt;
        </desc>
             

             <rect id="background" fill="#FFFFFF" x="0" ry="0" width="500" height="500"/>
             
                  
          
          <a href="https://listenbrainz.org/release/39eba5ab-7d1c-4b6f-a631-8474ec66e81b" target="_blank">
          
            <image
                x="0"
                y="0"
                width="332"
                height="332"
                preserveAspectRatio="xMidYMid slice"
                href="https://archive.org/download/mbid-d9315890-2091-4a44-acc5-f7d1ab703f2f/mbid-d9315890-2091-4a44-acc5-f7d1ab703f2f-21771955222_thumb500.jpg">
                
                <title>Lights Are On - Tom Rosenthal</title>
                
            </image>
          
          </a>
          

             
                  
          
          <a href="https://listenbrainz.org/release/439dbae6-863d-437a-a36c-68cbf1662399" target="_blank">
          
            <image
                x="332"
                y="0"
                width="168"
                height="166"
                preserveAspectRatio="xMidYMid slice"
                href="https://archive.org/download/mbid-6cf98d53-6e34-47f0-8b24-6ff6ed902b5b/mbid-6cf98d53-6e34-47f0-8b24-6ff6ed902b5b-30895504389_thumb500.jpg">
                
                <title>Dirty Thoughts - Chloe Adams</title>
                
            </image>
          
          </a>
          

             
                  
          
          <a href="https://listenbrainz.org/release/14707c5e-1b13-4e51-987c-68c90b15ff59" target="_blank">
          
            <image
                x="332"
                y="166"
                width="168"
                height="166"
                preserveAspectRatio="xMidYMid slice"
                href="https://archive.org/download/mbid-b02fa99a-d674-4bc1-8c4d-91d37601cf05/mbid-b02fa99a-d674-4bc1-8c4d-91d37601cf05-27123416806_thumb500.jpg">
                
                <title>Stereo Hearts - Gym Class Heroes feat. Adam Levine</title>
                
            </image>
          
          </a>
          

             
                  
          
          <a href="https://listenbrainz.org/release/1f215b13-829c-4077-9098-e287d1d87f50" target="_blank">
          
            <image
                x="0"
                y="332"
                width="166"
                height="168"
                preserveAspectRatio="xMidYMid slice"
                href="https://archive.org/download/mbid-a22cb9d6-bbb4-494b-8363-0835c1652351/mbid-a22cb9d6-bbb4-494b-8363-0835c1652351-17742326119_thumb500.jpg">
                
                <title>One Kiss - Calvin Harris &amp; Dua Lipa</title>
                
            </image>
          
          </a>
          

             
                  
          
          <a href="https://listenbrainz.org/release/a4fd6c50-4b1e-48e5-8139-9da17b20fa18" target="_blank">
          
            <image
                x="166"
                y="332"
                width="166"
                height="168"
                preserveAspectRatio="xMidYMid slice"
                href="https://archive.org/download/mbid-0e3c457b-cabb-4f22-9f60-56384f17dd83/mbid-0e3c457b-cabb-4f22-9f60-56384f17dd83-5738839329_thumb500.jpg">
                
                <title>Out of My League - Fitz and the Tantrums</title>
                
            </image>
          
          </a>
          

             
                  
          
          <a href="https://listenbrainz.org/release/9b5a4037-fee1-4233-9931-c9e8cdea04b8" target="_blank">
          
            <image
                x="332"
                y="332"
                width="168"
                height="168"
                preserveAspectRatio="xMidYMid slice"
                href="https://archive.org/download/mbid-935404f6-e8f9-48ca-9695-48c653401274/mbid-935404f6-e8f9-48ca-9695-48c653401274-32633687242_thumb500.jpg">
                
                <title>Treehouse - Alex G, Emily Yacina</title>
                
            </image>
          
          </a>
          

             
        </svg>
    """.trimIndent()
}