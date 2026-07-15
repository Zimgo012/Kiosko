package com.example.myapplication.viewmodel.menu

import androidx.lifecycle.ViewModel
import com.example.myapplication.controller.menu.AppController
import com.example.myapplication.controller.menu.AppScreen
import kotlinx.coroutines.flow.StateFlow

class AppViewModel : ViewModel() {
    private val appController = AppController()

    val currentScreen: StateFlow<AppScreen> = appController.currentScreen

    fun navigateTo(screen: AppScreen) {
        appController.navigateTo(screen)
    }
}
