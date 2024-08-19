package com.purered.pr1addemo.data.remote.dto

import com.purered.pr1addemo.domain.model.Article

data class NewsResponse(
    val articles: List<Article>,
    val status: String,
    val totalResults: Int
)