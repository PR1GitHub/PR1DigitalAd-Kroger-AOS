package com.purered.pr1digitaladclassic

import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

/**
 * Successful getPageDetails responses for ONE loaded ad.
 *
 * Freshness rules:
 * - Instances are created with remember(ad) in the ad views, so a fresh WeeklyAd fetch
 *   (retry, reload, new DigitalAd instance) always starts with an empty cache - cached
 *   page data can never outlive the ad content it belongs to.
 * - Failures are never cached: a page whose fetch failed retries on its next visit.
 */
internal class AdPageDetailsCache {
    private val mutex = Mutex()
    private val pagesById = mutableMapOf<String, AdPage>()

    suspend fun getOrFetch(pageId: String, fetch: suspend () -> AdPage): AdPage {
        mutex.withLock { pagesById[pageId] }?.let { return it }
        // Fetch outside the lock so one slow page never blocks the others. Two
        // concurrent fetches of the same page are harmless: identical payload,
        // last write wins.
        val fresh = fetch()
        mutex.withLock { pagesById[pageId] = fresh }
        return fresh
    }
}
