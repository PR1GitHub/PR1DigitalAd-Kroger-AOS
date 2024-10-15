package com.purered.pr1digitaladclassic

import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch

import androidx.lifecycle.ViewModel

internal  data class DigitalAdState(
    val loading : Boolean = true,
    val WeeklyAd : WeeklyAd?=null,
    val error : String? = null)


internal class DigitalAdViewModel: ViewModel()  {

    private val _weeklyAdState = mutableStateOf(DigitalAdState())

    val digitalAdState: State<DigitalAdState> = _weeklyAdState;

    private var eventId: Int? = null
    private var mode: String? = null


    private var adId:String?=null
    private var location:String?=null



    fun reloadWeeklyAd(){
        _weeklyAdState.value = _weeklyAdState.value.copy(loading = true)
        if(adId != null && location != null) {
            fetchAdDetails(this.adId!!, this.location!!)
        }
//        if(eventId != null && mode != null) {
//            fetchWeeklyAd(this.eventId!!, this.mode!!)
//        }
    }

//    fun fetchWeeklyAdById(eventId: String, mode: String) {
//        this.eventId = eventId
//        this.mode = mode
//
//        viewModelScope.launch {
//
//
//            try {
//                val response = weeklyAdService.getAdDetails(eventId, mode)
//                _weeklyAdState.value = _weeklyAdState.value.copy(
//                    loading = false,
//                    WeeklyAd = response,
//                    error = null
//                )
//
//
//            } catch (e: Exception) {
//
//                _weeklyAdState.value = _weeklyAdState.value.copy(
//                    loading = false,
//                    error = "Error fetching WeeklyAd ${e.message}"
//                )
//
//            }
//        }
//    }

    fun fetchWeeklyAd(eventId: Int, mode: String) {
        this.eventId = eventId
        this.mode = mode

        viewModelScope.launch{


            try {
                val response = weeklyAdService.getEventDetails(eventId,mode)
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




    fun fetchAdDetails(adId: String, location: String) {
        this.adId = adId
        this.location = location

        viewModelScope.launch{


            try {
//                println("Loading..!")
                val response = weeklyAdService.getAdDetails(adId,location)
//                println("#############################")
//                println(response)
                _weeklyAdState.value = _weeklyAdState.value.copy(
                    loading = false,
                    WeeklyAd = response,
                    error = null
                )


            }catch (e:Exception){
                println("Failed to fetching WeeklyAd")
                println(e)
                _weeklyAdState.value = _weeklyAdState.value.copy(
                    loading = false,
                    error = "Error fetching WeeklyAd ${e.message}"
                )

            }
        }




    }



}