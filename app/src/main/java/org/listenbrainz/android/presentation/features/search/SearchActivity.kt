package org.listenbrainz.android.presentation.features.search

import android.app.SearchManager
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.database.Cursor
import android.graphics.drawable.ColorDrawable
import android.net.Uri.parse
import android.os.Bundle
import android.provider.SearchRecentSuggestions
import android.view.Menu
import android.view.MenuItem
import android.view.View.*
import android.widget.ArrayAdapter
import android.widget.ListPopupWindow
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.SearchView
import androidx.core.view.isVisible
import androidx.cursoradapter.widget.CursorAdapter
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.paging.LoadState
import androidx.paging.PagingData
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.chip.Chip
import kotlinx.coroutines.launch
import org.listenbrainz.android.App
import org.listenbrainz.android.R
import org.listenbrainz.android.data.sources.Constants
import org.listenbrainz.android.data.sources.api.entities.mbentity.MBEntityType
import org.listenbrainz.android.databinding.ActivitySearchBinding
import org.listenbrainz.android.presentation.IntentFactory
import org.listenbrainz.android.presentation.features.adapters.ResultItem
import org.listenbrainz.android.presentation.features.adapters.ResultItemComparator
import org.listenbrainz.android.presentation.features.adapters.ResultPagingAdapter
import org.listenbrainz.android.presentation.features.barcode.BarcodeActivity
import org.listenbrainz.android.presentation.features.search.SearchPagingSource.Companion.loadResultCount
import org.listenbrainz.android.presentation.features.suggestion.SuggestionHelper
import org.listenbrainz.android.presentation.features.suggestion.SuggestionProvider

class SearchActivity : AppCompatActivity(), SearchView.OnQueryTextListener {

    private val defaultOffsetSize = 30 //Number of entities to be shown in a page
    private val firstPageOffset = 0

    private var suggestionHelper: SuggestionHelper? = null
    private var suggestionAdapter: CursorAdapter? = null
    private lateinit var binding: ActivitySearchBinding
    private var pageArray : ArrayList<Int> = arrayListOf()
    private var search_index = 0
    private var offset = 0
    private var viewModel: SearchViewModel? = null
    private var adapter: ResultPagingAdapter? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySearchBinding.inflate(layoutInflater)

        supportActionBar!!.setBackgroundDrawable(ColorDrawable(resources.getColor(R.color.app_bg)))
        supportActionBar!!.setDisplayHomeAsUpEnabled(true)
        setContentView(binding.root)

        setupSearchView()

        suggestionHelper = SuggestionHelper(this)
        suggestionAdapter = suggestionHelper!!.adapter
        binding.searchView.suggestionsAdapter = suggestionAdapter

        binding.recyclerView.layoutManager = LinearLayoutManager(this)
        viewModel = ViewModelProvider(this).get(SearchViewModel::class.java)

        binding.cardArtist.setOnClickListener {
            performAction(Constants.ADD_ARTIST)
        }

        binding.cardRelease.setOnClickListener {
            performAction(Constants.ADD_RELEASE)
        }
        binding.cardEvent.setOnClickListener {
            performAction(Constants.ADD_EVENT)
        }
        binding.cardReleaseGroup.setOnClickListener {
            performAction(Constants.ADD_RELEASEGROUP)
        }
        binding.cardLabel.setOnClickListener {
            performAction(Constants.ADD_LABEL)
        }

        binding.cardRecording.setOnClickListener {
            performAction(Constants.ADD_RECORDING)
        }

        binding.barcodeScan.setOnClickListener{
            startActivity(Intent(this, BarcodeActivity::class.java))
        }
        if (!packageManager.hasSystemFeature(PackageManager.FEATURE_CAMERA_ANY)) {
            binding.barcodeScan.visibility = GONE
        }
        val textArray = resources.getStringArray(R.array.searchType)
        for ((index, text) in textArray.withIndex()) {
            val chip = layoutInflater.inflate(R.layout.cat_chip_group_item_choice, binding.chipGroup, false) as Chip
            chip.text = text
            chip.isCloseIconVisible = false
            chip.setOnCheckedChangeListener { _, isChecked ->
                pageArray.clear()
                if (isChecked){
                    search_index = index
                }
            }
            binding.chipGroup.addView(chip)
            if(index==0){
                binding.chipGroup.check(chip.id)
            }
        }
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.dash, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.menu_preferences -> {
                startActivity(IntentFactory.getSettings(this))
                true
            }
            android.R.id.home -> {
                onBackPressed()
                true
            }
            R.id.menu_open_website -> {
                performAction(App.WEBSITE_BASE_URL + "search?type=" + searchTypeFromSpinner!!.entity + "&query=" + binding.searchView.query.toString())
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    private fun performAction(url: String){
        val intent = Intent(Intent.ACTION_VIEW)
        intent.data = parse(url)
        startActivity(intent)
    }

    private val searchTypeFromSpinner: MBEntityType? get() {
        return when (search_index) {
            0 -> MBEntityType.ARTIST
            1 -> MBEntityType.RELEASE
            2 -> MBEntityType.LABEL
            3 -> MBEntityType.RECORDING
            4 -> MBEntityType.EVENT
            5 -> MBEntityType.RELEASE_GROUP
            6 -> MBEntityType.INSTRUMENT
            else -> null
        }
    }

    private fun setupSearchView() {
        val searchManager = getSystemService(Context.SEARCH_SERVICE) as SearchManager
        binding.searchView.setSearchableInfo(searchManager.getSearchableInfo(componentName))
        binding.searchView.isSubmitButtonEnabled = true
        binding.searchView.setIconifiedByDefault(false)
        binding.searchView.setOnQueryTextListener(this)
        binding.searchView.setOnSuggestionListener(object : SearchView.OnSuggestionListener {
            override fun onSuggestionSelect(position: Int): Boolean {
                return false
            }

            override fun onSuggestionClick(position: Int): Boolean {
                val cursor = suggestionAdapter!!.getItem(position) as Cursor
                val query = cursor.getString(cursor.getColumnIndexOrThrow("display1"))
                binding.searchView.setQuery(query, false)
                return true
            }
        })
    }

    override fun onQueryTextSubmit(query: String): Boolean {
        if (App.context?.isOnline == false){
            Toast.makeText(this,"Connect to Internet and Try Again",Toast.LENGTH_LONG).show()
            return false
        }
        saveSearchSuggestion(query)
        binding.btnPageNumber.text = "1"//On a fresh search, the page size should change to 1.

        adapter = ResultPagingAdapter(ResultItemComparator(), searchTypeFromSpinner!!)
        binding.recyclerView.adapter = adapter
        adapter!!.resetAnimation()
        //Using SearchView for searching entity , represents the first page of the search results.
        viewModel!!.search(searchTypeFromSpinner, query, firstPageOffset).observe(this) { pagingData: PagingData<ResultItem> ->
            adapter!!.submitData(lifecycle, pagingData)
            offset = 0
            binding.btnLoadPrevSearches.isVisible = false
        }

        //Pressing btnLoadMoreSearches button to get to next page
        binding.btnLoadMoreSearches.setOnClickListener {
            viewModel!!.search(searchTypeFromSpinner, query, offset+defaultOffsetSize).observe(this)
            { pagingData: PagingData<ResultItem> ->
                adapter!!.submitData(lifecycle, pagingData)
                offset += defaultOffsetSize
                val currentPageNumber = (offset/defaultOffsetSize) + 1
                binding.btnPageNumber.text = (currentPageNumber).toString()
            }
        }

        //Pressing btnLoadPrevSearches button to get to previous page
        binding.btnLoadPrevSearches.setOnClickListener {
            viewModel!!.search(searchTypeFromSpinner, query, offset-defaultOffsetSize).observe(this)
            { pagingData: PagingData<ResultItem> ->
                adapter!!.submitData(lifecycle, pagingData)
                offset-=defaultOffsetSize
                val currentPageNumber = (offset/defaultOffsetSize) + 1
                binding.btnPageNumber.text = (currentPageNumber).toString()
            }
        }
        lifecycleScope.launch {
            //Companion Object's loadResultCount was dealt with here, as otherwise the variable was accessed before it was reassigned.
            adapter!!.loadStateFlow.collect {loadState->

                binding.loadingAnimation.isVisible = loadState.refresh is LoadState.Loading
                binding.noResult.isVisible = loadState.refresh is LoadState.Error
                binding.btnLoadMoreSearches.isVisible = offset +defaultOffsetSize< loadResultCount
                binding.btnLoadPrevSearches.isVisible = offset >= defaultOffsetSize
                binding.btnsPageNav.isVisible = when {
                    loadState.refresh is LoadState.Error -> false
                    loadResultCount<defaultOffsetSize -> false
                    else -> loadState.refresh !is LoadState.Loading
                }
                binding.recyclerView.isVisible = if(loadState.refresh is LoadState.Error) false
                                    else loadState.refresh !is LoadState.Loading

                //Setting the pages menu
                val listPopupWindow = ListPopupWindow(this@SearchActivity,null,R.attr.listPopupWindowStyle)
                listPopupWindow.anchorView = binding.btnPageNumber
                val pageAdapter = ArrayAdapter(this@SearchActivity,R.layout.dropdown_page,pageArray)
                listPopupWindow.setAdapter(pageAdapter)

                // Show list popup window on button click.
                binding.btnPageNumber.setOnClickListener {
                    if (pageArray.isEmpty()) {
                        for (pageNumber in 1..loadResultCount/defaultOffsetSize){
                            pageArray.add(pageNumber)
                        }
                        pageArray.add(pageArray.lastIndex+2)
                    }
                    listPopupWindow.show()
                  }
                //Menu items clicks handled here
                listPopupWindow.setOnItemClickListener { _, _, i, _ ->
                    viewModel!!.search(searchTypeFromSpinner, query, (pageArray[i]-1)*defaultOffsetSize).observe(this@SearchActivity) { pagingData: PagingData<ResultItem> ->
                        adapter!!.submitData(lifecycle, pagingData)
                        offset = (pageArray[i]-1)*defaultOffsetSize
                        binding.btnPageNumber.text = pageArray[i].toString()
                    }
                }
            }
        }
        binding.recyclerView.visibility = VISIBLE
        binding.gridView.visibility = GONE

        binding.searchView.clearFocus()
        return true
    }

    private fun saveSearchSuggestion(query: String?) {
        val suggestions = SearchRecentSuggestions(this, SuggestionProvider.AUTHORITY, SuggestionProvider.MODE)
        suggestions.saveRecentQuery(query, null)
    }

    override fun onQueryTextChange(newText: String): Boolean {
        pageArray.clear()
        when {
            newText.isEmpty() -> {
                binding.noResult.visibility = GONE
                binding.loadingAnimation.visibility = GONE
                binding.recyclerView.visibility = GONE
                binding.gridView.visibility = VISIBLE
                binding.btnsPageNav.visibility = GONE
            }
            else -> {
                suggestionAdapter!!.changeCursor(suggestionHelper!!.getMatchingEntries(newText))
            }
        }
        return false
    }

    override fun onBackPressed() {
        when {
            binding.noResult.isVisible -> {
                binding.gridView.visibility = VISIBLE
                binding.recyclerView.visibility = GONE
                binding.btnsPageNav.visibility = GONE
                binding.loadingAnimation.visibility = GONE
                binding.noResult.visibility = GONE
            }
            else -> {
                super.onBackPressed()
            }
        }
    }
}
//1.Add Artist:   https://musicbrainz.org/artist/create?edit-artist2.Add Release: https://musicbrainz.org/release/add3.Add Event: https://musicbrainz.org/event/create?edit-event4.Add Release group:https://musicbrainz.org/release-group/create?edit-release-group5.Add label: https://musicbrainz.org/label/create?edit-label6.Add recording: https://musicbrainz.org/recording/create?edit-recording