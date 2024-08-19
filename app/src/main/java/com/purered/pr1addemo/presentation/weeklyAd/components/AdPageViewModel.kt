package com.purered.pr1addemo.presentation.weeklyAd.components

import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import com.purered.pr1addemo.data.repository.WeeklyAd
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.compose.viewModel
import com.purered.pr1addemo.data.repository.AdPage
import com.purered.pr1addemo.data.repository.weeklyAdService
import com.purered.pr1addemo.presentation.weeklyAd.DigitalAdState
import com.purered.pr1addemo.presentation.weeklyAd.WeeklyAdsViewModel
import kotlinx.coroutines.launch






data class AdPageState(
    val loading: Boolean = true,
    val AdPage: AdPage? = null,
    val error: String? = null
)

class AdPageViewModel: ViewModel() {


    private val _adPageState = mutableStateOf(AdPageState())

    val adPageState: State<AdPageState> = _adPageState;

    private var eventId: Int? = null
    private var pageId: String? = null


    fun reloadAdPage() {
        _adPageState.value = _adPageState.value.copy(loading = true)
        if (eventId != null && pageId != null) {
            fetchAdPage(this.eventId!!, this.pageId!!)
        }
    }

    fun fetchAdPage(eventId: Int, pageId: String) {
        this.eventId = eventId
        this.pageId = pageId

        viewModelScope.launch {
            try {
                val response = weeklyAdService.getPageDetails(eventId, pageId, "public")
                println(response)
                _adPageState.value = _adPageState.value.copy(
                    loading = false,
                    AdPage = response,
                    error = null
                )
            } catch (e: Exception) {
                _adPageState.value = _adPageState.value.copy(
                    loading = false,
                    error = "Error fetching AdPage ${e.message}"
                )
            }
        }
    }
}