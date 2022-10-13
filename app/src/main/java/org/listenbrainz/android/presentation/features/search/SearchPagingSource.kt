package org.listenbrainz.android.presentation.features.search


import androidx.paging.PagingSource
import androidx.paging.PagingState
import com.google.gson.JsonParser
import org.listenbrainz.android.data.sources.api.MusicBrainzServiceGenerator
import org.listenbrainz.android.data.sources.api.SearchService
import org.listenbrainz.android.data.sources.api.entities.mbentity.MBEntityType
import org.listenbrainz.android.presentation.features.adapters.ResultItem
import org.listenbrainz.android.presentation.features.adapters.ResultItemUtils

class SearchPagingSource(val mbEntityType: MBEntityType, val query: String , var offset : Int) : PagingSource<Int, ResultItem>() {

    companion object{
        //Using loadResultCount to access the data size of the response in the SearchActivity.
        var loadResultCount : Int = 0
    }

    private val service = MusicBrainzServiceGenerator.createService(SearchService::class.java, true)

    override suspend fun load(params: LoadParams<Int>): LoadResult<Int, ResultItem> {
        val pageSize: Int = params.loadSize
        if (offset==0){
            if (params.key!=null){
                offset = params.key!!
            }
        }
        return try {
            val response = service.searchEntity(mbEntityType.entity, query, pageSize, offset)?.string()
            var count = LoadResult.Page.COUNT_UNDEFINED
            val data = ResultItemUtils.getJSONResponseAsResultItemList(response, mbEntityType)
            if (offset == 0) {
                val responseObject = JsonParser.parseString(response)
                count = responseObject.asJsonObject.get("count").asInt
                loadResultCount = count
            }
            val itemsAfter = when (count) {
                LoadResult.Page.COUNT_UNDEFINED -> count
                else -> (count - offset - pageSize).coerceAtLeast(0)
            }
            // itemsAfter is required to be at least otherwise the current page will be not loaded

            if (offset==0 && data.isEmpty() && count==0){
                throw ArrayIndexOutOfBoundsException()
            }
            LoadResult.Page(
                data = data,
                prevKey = null,
                nextKey = null,
                itemsAfter = itemsAfter
            )
        } catch (e: Exception) {
            e.printStackTrace()
            LoadResult.Error(e)
        }
    }

    override fun getRefreshKey(state: PagingState<Int, ResultItem>): Int? {
        return state.anchorPosition?.let { anchorPosition ->
            val anchorPage = state.closestPageToPosition(anchorPosition)
            anchorPage?.prevKey?.plus(1) ?: anchorPage?.nextKey?.minus(1)
        }
    }
}