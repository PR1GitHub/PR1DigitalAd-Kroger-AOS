package com.purered.pr1addemo.domain.usecases.news

import com.purered.pr1addemo.data.local.NewsDao
import com.purered.pr1addemo.domain.model.Article
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class GetSavedArticles @Inject constructor(
    private val newsDao: NewsDao
) {

    operator fun invoke(): Flow<List<Article>>{
        return newsDao.getArticles()
    }

}