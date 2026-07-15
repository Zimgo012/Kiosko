package com.example.myapplication

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.example.myapplication.controller.menu.AppScreen
import com.example.myapplication.engine.camera.CameraController
import com.example.myapplication.engine.printer.PrinterManager
import com.example.myapplication.ui.booth.BoothUI
import com.example.myapplication.ui.home.HomeScreen
import com.example.myapplication.ui.theme.MyApplicationTheme
import com.example.myapplication.viewmodel.booth.BoothViewModel
import com.example.myapplication.viewmodel.menu.AppViewModel

class MainActivity : ComponentActivity() {

    private val appViewModel by viewModels<AppViewModel>()
    private val boothViewModel by viewModels<BoothViewModel>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        // Initialize Printer Manager
        boothViewModel.initPrinter(PrinterManager(this))

        if (!hasRequiredPermissions()) {
            ActivityCompat.requestPermissions(
                this, CAMERAX_PERMISSIONS, 0
            )
        }

        enableEdgeToEdge()
        setContent {
            MyApplicationTheme {
                val currentScreen by appViewModel.currentScreen.collectAsState()
                
                val controller = remember {
                    CameraController(
                        applicationContext,
                        onPhotoCaptured = boothViewModel::onTakePhoto
                    )
                }

                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    when (currentScreen) {
                        AppScreen.Home -> {
                            HomeScreen(
                                onStartCamera = { appViewModel.navigateTo(AppScreen.Camera) },
                                modifier = Modifier.padding(innerPadding)
                            )
                        }
                        AppScreen.Camera -> {
                            BoothUI(
                                cameraController = controller,
                                viewModel = boothViewModel,
                                modifier = Modifier.padding(innerPadding),
                                onBack = { appViewModel.navigateTo(AppScreen.Home) }
                            )
                        }
                    }
                }
            }
        }
    }

    private fun hasRequiredPermissions(): Boolean {
        return CAMERAX_PERMISSIONS.all {
            ContextCompat.checkSelfPermission(
                baseContext,
                it
            ) == PackageManager.PERMISSION_GRANTED
        }
    }

    companion object {
        private val CAMERAX_PERMISSIONS = arrayOf(
            Manifest.permission.CAMERA
        )
    }
}
