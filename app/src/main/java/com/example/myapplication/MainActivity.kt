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
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.example.myapplication.engine.camera.CameraController
import com.example.myapplication.engine.camera.CameraViewModel
import com.example.myapplication.engine.printer.PrinterManager
import com.example.myapplication.ui.booth.CameraScreen
import com.example.myapplication.ui.home.HomeScreen
import com.example.myapplication.ui.theme.MyApplicationTheme

enum class AppScreen { Home, Camera }

class MainActivity : ComponentActivity() {

    private val viewModel by viewModels<CameraViewModel>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        // Initialize Printer Manager
        viewModel.initPrinter(PrinterManager(this))

        if (!hasRequiredPermissions()) {
            ActivityCompat.requestPermissions(
                this, CAMERAX_PERMISSIONS, 0
            )
        }

        enableEdgeToEdge()
        setContent {
            MyApplicationTheme {
                var currentScreen by remember { mutableStateOf(AppScreen.Home) }
                
                val controller = remember {
                    CameraController(
                        applicationContext,
                        onPhotoCaptured = viewModel::onTakePhoto
                    )
                }

                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    when (currentScreen) {
                        AppScreen.Home -> {
                            HomeScreen(
                                onStartCamera = { currentScreen = AppScreen.Camera },
                                modifier = Modifier.padding(innerPadding)
                            )
                        }
                        AppScreen.Camera -> {
                            CameraScreen(
                                cameraController = controller,
                                viewModel = viewModel,
                                modifier = Modifier.padding(innerPadding),
                                onBack = { currentScreen = AppScreen.Home }
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
