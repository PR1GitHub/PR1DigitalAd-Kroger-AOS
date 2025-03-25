package com.purered.pr1digitaladclassic


import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel

class HotMapViewModel : ViewModel() {
    var isZooming by mutableStateOf(false)
        private set

    var isZoomStarted by mutableStateOf(false)
        private set

    var isZoomCompleted by mutableStateOf(false)
        private set

    fun onZoomStart() {
        isZooming = true
        isZoomStarted = true
        isZoomCompleted = false
    }

    fun onZoomEnd() {
        isZooming = false
        isZoomStarted = false
        isZoomCompleted = true
    }
}