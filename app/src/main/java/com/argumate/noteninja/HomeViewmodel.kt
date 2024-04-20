package com.argumate.noteninja

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow

class HomeViewmodel : ViewModel() {
    var bpm = MutableStateFlow(60f)
    var scale = MutableStateFlow(0)
    var key = MutableStateFlow(24)
    val startNote = MutableStateFlow(21)
    val endNote = MutableStateFlow(28)

    val onValueChange = { value: Float ->
        bpm.value = value
    }

    val onScaleChange = { value: Int ->
        scale.value = value
    }

    val updateNote = {
        key.value = (startNote.value..endNote.value).random()
    }

    val updateStartNote = { value: Int ->
        startNote.value = value
        if (startNote.value > endNote.value) {
            endNote.value = startNote.value
        }
    }

    val updateEndNote = { value: Int ->
        endNote.value = value
    }
}