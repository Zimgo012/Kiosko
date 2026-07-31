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
import com.example.myapplication.engine.storage.StorageManager
import com.example.myapplication.ui.admin.AdminScreen
import com.example.myapplication.ui.booth.BoothUI
import com.example.myapplication.ui.home.HomeScreen
import com.example.myapplication.ui.theme.MyApplicationTheme
import com.example.myapplication.viewmodel.admin.AdminViewModel
import com.example.myapplication.viewmodel.booth.BoothViewModel
import com.example.myapplication.viewmodel.menu.AppViewModel

class MainActivity : ComponentActivity() {

    private val appViewModel by viewModels<AppViewModel>()
    private val boothViewModel by viewModels<BoothViewModel>()
    private val adminViewModel by viewModels<AdminViewModel>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        // Initialize Printer and Storage Managers
        boothViewModel.initPrinter(PrinterManager(this))
        boothViewModel.initStorage(StorageManager(this))

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
                                onOpenAdmin = { appViewModel.navigateTo(AppScreen.Admin) },
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
                        AppScreen.Admin -> {
                            val printSettings by boothViewModel.printTemplateSettings.collectAsState()
                            val availableFolders by boothViewModel.availableFolders.collectAsState()
                            val isAutoStartEnabled by boothViewModel.isAutoStartEnabled.collectAsState()
                            val selectedFrame by boothViewModel.selectedFrame.collectAsState()
                            
                            AdminScreen(
                                viewModel = adminViewModel,
                                settings = printSettings,
                                availableFolders = availableFolders,
                                isAutoStartEnabled = isAutoStartEnabled,
                                selectedFrame = selectedFrame,
                                cameraController = controller,
                                onSettingsChange = boothViewModel::updatePrintTemplate,
                                onToggleAutoStart = boothViewModel::toggleAutoStart,
                                onFrameTypeChange = boothViewModel::setFrameType,
                                onRefreshFolders = boothViewModel::refreshFolders,
                                onTestPrint = boothViewModel::testPrint,
                                onBack = { appViewModel.navigateTo(AppScreen.Home) },
                                modifier = Modifier.padding(innerPadding)
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
