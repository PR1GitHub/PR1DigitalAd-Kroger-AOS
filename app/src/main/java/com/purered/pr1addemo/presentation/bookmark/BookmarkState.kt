package com.purered.pr1addemo.presentation.bookmark

import com.purered.pr1addemo.domain.model.Article

data class BookmarkState(
    val articles: List<Article> = emptyList()
)