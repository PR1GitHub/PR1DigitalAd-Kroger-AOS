package com.purered.pr1addemo.presentation.search

import androidx.paging.PagingData
import com.purered.pr1addemo.domain.model.Article
import kotlinx.coroutines.flow.Flow

data class SearchState(
    val searchQuery: String = "",
    val articles: Flow<PagingData<Article>>? = null
)