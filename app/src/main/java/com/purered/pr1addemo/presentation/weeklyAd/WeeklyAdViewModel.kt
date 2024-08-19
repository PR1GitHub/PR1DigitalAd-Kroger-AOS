package com.purered.pr1addemo.presentation.weeklyAd

import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.purered.pr1addemo.data.repository.WeeklyAd
import com.purered.pr1addemo.data.repository.weeklyAdService
import kotlinx.coroutines.launch

data class DigitalAdState(
    val loading : Boolean = true,
    val WeeklyAd : WeeklyAd?=null,
    val error : String? = null)


class WeeklyAdsViewModel: ViewModel() {


    private val _weeklyAdState = mutableStateOf(DigitalAdState())

    val weeklyAdState:State<DigitalAdState> = _weeklyAdState;

    private var eventId: Int? = null
    private var mode: String? = null



    fun reloadWeeklyAd(){
        _weeklyAdState.value = _weeklyAdState.value.copy(loading = true)
        if(eventId != null && mode != null) {
            fetchWeeklyAd(this.eventId!!, this.mode!!)
         }
    }



      fun fetchWeeklyAd(eventId: Int, mode: String) {
          this.eventId = eventId
          this.mode = mode

        viewModelScope.launch{


            try {
                val response = weeklyAdService.getEventDetails(eventId,mode)
                println(response)
                _weeklyAdState.value = _weeklyAdState.value.copy(
                    loading = false,
                    WeeklyAd = response,
                    error = null
                )


            }catch (e:Exception){

                _weeklyAdState.value = _weeklyAdState.value.copy(
                    loading = false,
                    error = "Error fetching WeeklyAd ${e.message}"
                )

            }
        }




    }



}
