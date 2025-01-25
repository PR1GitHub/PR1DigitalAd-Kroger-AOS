package com.purered.pr1digitaladclassic

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.geometry.Offset

class ZoomState {
    var isZooming by mutableStateOf(false)
    var scale by mutableStateOf(1f)
    var offset by mutableStateOf(Offset.Zero)
}