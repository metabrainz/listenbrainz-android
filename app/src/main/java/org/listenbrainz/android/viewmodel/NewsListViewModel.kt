package org.listenbrainz.android.viewmodel

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import org.listenbrainz.android.model.BlogPost
import org.listenbrainz.android.repository.blog.BlogRepository
import org.listenbrainz.android.util.Resource
import org.listenbrainz.android.util.Resource.Status.SUCCESS
import javax.inject.Inject

@HiltViewModel
class NewsListViewModel @Inject constructor(val repository: BlogRepository) : ViewModel() {
    var isLoading: Boolean  by mutableStateOf(true)
    private val _blogPostsFlow = MutableStateFlow(emptyList<BlogPost>())
    val blogPostsFlow = _blogPostsFlow.asStateFlow()

    fun fetchBlogs() {
        viewModelScope.launch {
            val response = repository.fetchBlogs()
            when (response.status) {
                SUCCESS -> {
                    val responseBlogs = response.data!!
                    // Updating blogs
                    _blogPostsFlow.update { responseBlogs.posts }
                    isLoading = false
                }

                Resource.Status.LOADING -> {
                    isLoading = true
                }

                Resource.Status.FAILED -> {
                    isLoading = false
                }
            }
        }
    }
}