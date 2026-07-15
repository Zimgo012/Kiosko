package com.example.myapplication.controller.booth

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

enum class FrameType(val title: String, val rows: Int, val cols: Int) {
    SINGLE("1 x 1", 1, 1),
    QUAD("2 x 2", 2, 2),
    SIX("3 x 2", 3, 2);

    val photoCount: Int get() = rows * cols
}

class SettingsController {
    private val _selectedFrame = MutableStateFlow(FrameType.SINGLE)
    val selectedFrame = _selectedFrame.asStateFlow()

    private val _isAutoStartEnabled = MutableStateFlow(true)
    val isAutoStartEnabled = _isAutoStartEnabled.asStateFlow()

    fun setFrameType(frameType: FrameType) {
        _selectedFrame.value = frameType
    }

    fun toggleAutoStart() {
        _isAutoStartEnabled.value = !_isAutoStartEnabled.value
    }
}
