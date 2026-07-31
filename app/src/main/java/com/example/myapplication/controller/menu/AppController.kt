package com.example.myapplication.controller.menu

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

enum class AppScreen { Home, Camera, Admin }

class AppController {
    private val _currentScreen = MutableStateFlow(AppScreen.Home)
    val currentScreen = _currentScreen.asStateFlow()

    fun navigateTo(screen: AppScreen) {
        _currentScreen.value = screen
    }
}
