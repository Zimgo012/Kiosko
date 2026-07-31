package com.example.myapplication.controller.admin

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

class AdminController {
    private val _isCloudSyncEnabled = MutableStateFlow(false)
    val isCloudSyncEnabled = _isCloudSyncEnabled.asStateFlow()

    fun toggleCloudSync() {
        _isCloudSyncEnabled.value = !_isCloudSyncEnabled.value
    }
    
    // Future sync-cloud API hooks can be added here
}
