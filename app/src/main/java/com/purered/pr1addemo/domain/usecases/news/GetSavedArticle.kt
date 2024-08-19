package com.purered.pr1addemo.domain.usecases.news

import com.purered.pr1addemo.data.local.NewsDao
import com.purered.pr1addemo.domain.model.Article
import javax.inject.Inject

class GetSavedArticle @Inject constructor(
    private val newsDao: NewsDao
) {

    suspend operator fun invoke(url: String): Article?{
        return newsDao.getArticle(url = url)
    }

}