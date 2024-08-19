package com.purered.pr1addemo.domain.usecases.news

import androidx.paging.PagingData
import com.purered.pr1addemo.domain.model.Article
import com.purered.pr1addemo.domain.repository.NewsRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class SearchNews @Inject constructor(
    private val newsRepository: NewsRepository
) {
    operator fun invoke(searchQuery: String, sources: List<String>): Flow<PagingData<Article>> {
        return newsRepository.searchNews(
            searchQuery = searchQuery,
            sources = sources
        )
    }
}