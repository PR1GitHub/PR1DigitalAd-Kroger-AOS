package com.purered.pr1digitaladclassic

import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch

internal  data class DigitalAdState(
    val loading : Boolean = true,
    val weeklyAd : WeeklyAd?=null,
    val error : String? = null
)


internal class DigitalAdViewModel: ViewModel()  {

    private val _weeklyAdState = mutableStateOf(DigitalAdState())

    val digitalAdState: State<DigitalAdState> = _weeklyAdState

    private var adId:String?=null
    private var location:String?=null

    internal var logEnabled: Boolean=false


    fun reloadWeeklyAd(){
        _weeklyAdState.value = _weeklyAdState.value.copy(loading = true)
        if(adId != null && location != null) {
            fetchAdDetails(this.adId!!, this.location!!)

            val logData = SaveLogs(SaveLogDetails(
                adId = this.adId!!, loc = this.location!!,
                appDetails = "AOS:[RELOAD-LOG]  [DigitalAdViewModel.kt]  reloadWeeklyAd() triggered... {adId: $adId, location: $location}"
            ))
            Logger.i("[RELOAD-LOG]  [DigitalAdViewModel.kt]  reloadWeeklyAd() triggered...", saveLogs = logData, sendToDB = false)
        }
    }

    fun fetchAdDetails(adId: String, location: String) {
        this.adId = adId
        this.location = location

        Logger.i("[API-LOG]  [DigitalAdViewModel.kt]  Entered fetchAdDetails()...", saveLogs = null, sendToDB = false)

        val logData = SaveLogs(SaveLogDetails(appDetails = "AOS:[API-LOG] [DigitalAdViewModel.kt]  getAdDetails Api triggered... {adId: $adId, location: $location, apiRequest = https://oms-kroger-webapp-da-classic-api-prod.przone.net/api/dacs/$adId?location=$location}"))
        Logger.i("${logData.value.appDetails}", saveLogs = logData, sendToDB = false)

        viewModelScope.launch{

            try {
                val response = weeklyAdService.getAdDetails(adId,location)

                _weeklyAdState.value = _weeklyAdState.value.copy(
                    loading = false,
                    weeklyAd = response,
                    error = null
                )

                logEnabled = response.isLogEnabled

                val logData = SaveLogs(SaveLogDetails(appDetails = "AOS:[API-LOG]  [DigitalAdViewModel.kt]  getAdDetails Api SUCCESS {adId: $adId, location: $location, apiRequest = https://oms-kroger-webapp-da-classic-api-prod.przone.net/api/dacs/$adId?location=$location}}"))
                Logger.i("${logData.value.appDetails}", saveLogs = logData, sendToDB = logEnabled)

                val logData1 = SaveLogs(SaveLogDetails(appDetails = "AOS:[API-LOG]  [DigitalAdViewModel.kt]  getAdDetails api response : $response"))
                Logger.i("${logData1.value.appDetails}", saveLogs = logData1, sendToDB = logEnabled)

                val logData2 = SaveLogs(SaveLogDetails(appDetails = "AOS:[API-LOG]  isLogEnabled = ${response.isLogEnabled}"))
                Logger.i("${logData2.value.appDetails}", saveLogs = logData2, sendToDB = logEnabled)

                val logData3 = SaveLogs(SaveLogDetails(appDetails = "AOS:[API-LOG]  getAdDetails > Total Ad Pages = ${response.pages.count()}"))
                Logger.i("${logData3.value.appDetails}", saveLogs = logData3, sendToDB = logEnabled)

            } catch (e:Exception){
                println("Failed to fetching WeeklyAd")
                println(e)
                _weeklyAdState.value = _weeklyAdState.value.copy(
                    loading = false,
                    error = "Error fetching WeeklyAd ${e.message}"
                )

                val logData = SaveLogs(SaveLogDetails(
                    adId = adId, loc = location,
                    appDetails = "AOS:[API-LOG]  [DigitalAdViewModel.kt]  getAdDetails Api FAILED {adId: $adId, location: $location, apiRequest = https://oms-kroger-webapp-da-classic-api-prod.przone.net/api/dacs/$adId?location=$location}}"
                ))
                Logger.e("${logData.value.appDetails}", saveLogs = logData, sendToDB = logEnabled)

            }
        }

    }

}