package com.purered.pr1digitaladclassic

import android.util.Log
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

    internal var logEnabled: Boolean=false


    fun reloadWeeklyAd(){
        _weeklyAdState.value = _weeklyAdState.value.copy(loading = true)
        if(adId != null && location != null) {
            //Logger.i("[RELOAD-LOG]  reloadWeeklyAd() triggered...", saveLogs = null)
            fetchAdDetails(this.adId!!, this.location!!)

            val logData = SaveLogs(SaveLogDetails(
                adId = this.adId!!,
                loc = this.location!!,
                offerId = "",
                url = "",
                appDetails = "AOS:[RELOAD-LOG] [DigitalAdViewModel.kt]  reloadWeeklyAd() triggered... {adId: $adId, location: $location}"
            ))
            Logger.i("[RELOAD-LOG] [DigitalAdViewModel.kt]  reloadWeeklyAd() triggered...", saveLogs = logData, sendToDB = true)
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

        Logger.i("[API-LOG] [DigitalAdViewModel.kt]  Entered fetchAdDetails()...", saveLogs = null, sendToDB = false)

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

                logEnabled = response.isLogEnabled
                //Log.d("[SDK-PARAMS]","response.isLogEnabled = ${response.isLogEnabled}")
                //Log.d("[SDK-PARAMS]","{initial} GlobalConfig.saveLogEnabled = ${GlobalConfig.saveLogEnabled}")
//                GlobalConfig.saveLogEnabled = response.isLogEnabled
                //Log.d("[SDK-PARAMS]","{value from api} GlobalConfig.saveLogEnabled = ${GlobalConfig.saveLogEnabled}")

                //if(logEnabled) {
                    Log.d("[SDK-PARAMS]","enter condition to save logs")
                    val logData = SaveLogs(SaveLogDetails(
                        adId = adId,
                        loc = location,
                        offerId = "",
                        url = "",
                        appDetails = "AOS:[API-LOG] [DigitalAdViewModel.kt]  fetchAdDetails Api SUCCESS {adId: $adId, location: $location}"
                    ))
                    Logger.i("[API-LOG] [DigitalAdViewModel.kt]  getAdDetails Api SUCCESS {adId: $adId, location: $location}", saveLogs = logData, sendToDB = logEnabled)
                //}

            }catch (e:Exception){
                println("Failed to fetching WeeklyAd")
                println(e)
                _weeklyAdState.value = _weeklyAdState.value.copy(
                    loading = false,
                    error = "Error fetching WeeklyAd ${e.message}"
                )

                val logData = SaveLogs(SaveLogDetails(
                    adId = adId,
                    loc = location,
                    offerId = "",
                    url = "",
                    appDetails = "AOS:[API-LOG] [DigitalAdViewModel.kt]  fetchAdDetails Api FAILED {adId: $adId, location: $location}"
                ))
                Logger.e("[API-LOG] [DigitalAdViewModel.kt]  fetchAdDetails Api FAILED {adId: $adId, location: $location}", saveLogs = logData, sendToDB = logEnabled)

            }
        }




    }



}