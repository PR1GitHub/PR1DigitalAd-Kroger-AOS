package com.purered.pr1digitaladclassic

import android.util.Log
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

object Logger {
    var isLoggingEnabled = true // Set to `true` to enable logs, `false` to disable them
    //var isApiLoggingEnabled = true // Enable/disable saving logs to DB
    //var isApiLoggingEnabled = GlobalConfig.saveLogEnabled

    //private lateinit var adService: weeklyAdService
    private val coroutineScope = CoroutineScope(Dispatchers.IO)

    var tagStr = "PR1DigitalAd-AOS-SDK"

    fun d(message: String, saveLogs: SaveLogs?, sendToDB: Boolean) {
        if (isLoggingEnabled) {
            Log.d(tagStr, message)
            //saveLogToDB(saveLogs)
        }
        if (sendToDB) { saveLogToDB(saveLogs) }
    }

    fun e(message: String, throwable: Throwable? = null, saveLogs: SaveLogs?, sendToDB: Boolean) {
        if (isLoggingEnabled) {
            Log.e(tagStr, message, throwable)
            //saveLogToDB(saveLogs)
        }
        //Log.d("[SDK-PARAMS]","fun e : sendToDB = $sendToDB")
        if (sendToDB) { saveLogToDB(saveLogs) }
    }

    fun i(message: String, saveLogs: SaveLogs?, sendToDB: Boolean) {
        if (isLoggingEnabled) {
            Log.i(tagStr, message)
            //saveLogToDB(saveLogs)
        }
        //Log.d("[SDK-PARAMS]","fun i : sendToDB = $sendToDB")
        if (sendToDB) { saveLogToDB(saveLogs) }
    }

    // Add similar methods for `Log.w`, `Log.v`, etc., if needed.

    private fun saveLogToDB(saveLogs: SaveLogs? = null) {
        if (saveLogs == null) return

        Log.i(tagStr, "[API-LOG] [Logger.kt]  Calling savelogs Api...")

        coroutineScope.launch {
            try {
                weeklyAdService.saveLogDetails(saveLogs)
                Log.i(tagStr, "[API-LOG] [Logger.kt]  Log saved successfully: ${saveLogs.value}")
            } catch (e: Exception) {
                Log.i(tagStr, "[API-LOG] [Logger.kt]  Log saving failed: ${e.message}")
            }
        }
    }
}