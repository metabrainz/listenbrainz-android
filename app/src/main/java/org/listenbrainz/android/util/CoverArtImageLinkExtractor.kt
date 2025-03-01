package org.listenbrainz.android.util

//Cover art returns a SVG image with multiple anchor tags, each containing an image link
//This class extracts the image links from the SVG
object CoverArtImageLinkExtractor {

    // Function to count the number of images inside anchor tags
    fun getImageCount(svg: String): Int {
        val regex = "<a\\s+href=.*?>\\s*<image\\s+".toRegex()
        return regex.findAll(svg).count()
    }

    // Function to extract image links inside <a> tags
    fun extractImageLinks(svg: String): List<String> {
        val regex = """<a\s+href=.*?>\s*<image[^>]+?href=["'](.*?)["']""".toRegex()
        return regex.findAll(svg).map { it.groupValues[1] }.toList()
    }

    // Function to extract anchor links from <a> tags
    fun extractAnchorLinks(svg: String): List<String> {
        val regex = """<a\s+href=["'](.*?)["']""".toRegex()
        return regex.findAll(svg).map { it.groupValues[1] }.toList()
    }
}
