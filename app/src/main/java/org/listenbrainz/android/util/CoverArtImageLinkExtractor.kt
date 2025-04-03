package org.listenbrainz.android.util

//Cover art returns a SVG image with multiple anchor tags, each containing an image link
//This class extracts the image links from the SVG
object CoverArtImageLinkExtractor {

    //We are extracting no of images by counting <image> tags inside anchor tags.
    fun getImageCount(svg: String): Int {
        var count = 0
        var index = 0

        while (true) {
            index = svg.indexOf("<a", index)
            if (index == -1) break  // No more <a> tags

            val endIndex = svg.indexOf("</a>", index)
            if (endIndex == -1) break  // Invalid tag structure

            // Look for <image> tags within the <a> tag
            var imageIndex = index
            while (true) {
                imageIndex = svg.indexOf("<image", imageIndex)
                if (imageIndex == -1 || imageIndex > endIndex) break
                count++
                imageIndex++
            }

            index = endIndex
        }

        return count
    }

    // In this function, we are extracting image links from <image> tags
    fun extractImageLinks(svg: String): List<String> {
        val imageLinks = mutableListOf<String>()
        var index = 0

        while (true) {
            index = svg.indexOf("<image", index)
            if (index == -1) break  // No more <image> tags, exit the loop

            val hrefIndex = svg.indexOf("href=\"", index)
            if (hrefIndex != -1) {
                val start = hrefIndex + 6  // Skip 'href="', i.e. move to start of the link
                val end = svg.indexOf("\"", start)
                if (end != -1) {
                    imageLinks.add(svg.substring(start, end))
                }
            }
            index++ // Move to the next occurrence of image tag
        }

        return imageLinks
    }

    //In this function we are extracting anchor links from <a> tags, which is used to make the images clickable
    fun extractAnchorLinks(svg: String): List<String> {
        val anchorLinks = mutableListOf<String>()
        var index = 0
        while (true) {
            index = svg.indexOf("<a", index)
            if (index == -1) break  // No more <a> tags

            val hrefIndex = svg.indexOf("href=\"", index)
            if (hrefIndex != -1) {
                val start = hrefIndex + 6  // Skip 'href="'
                val end = svg.indexOf("\"", start)
                if (end != -1) {
                    anchorLinks.add(svg.substring(start, end))
                }
            }
            index++
        }
        return anchorLinks
    }
}
