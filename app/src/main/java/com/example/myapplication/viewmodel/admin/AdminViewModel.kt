package com.example.myapplication.viewmodel.admin

import android.app.Application
import android.content.Context
import android.hardware.usb.UsbManager
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.core.content.ContextCompat
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.myapplication.controller.admin.AdminController
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class AdminViewModel(application: Application) : AndroidViewModel(application) {
    private val adminController = AdminController()

    val isCloudSyncEnabled: StateFlow<Boolean> = adminController.isCloudSyncEnabled

    private val _printerStatus = MutableStateFlow("Unknown")
    val printerStatus: StateFlow<String> = _printerStatus

    private val _cameraStatus = MutableStateFlow("Checking...")
    val cameraStatus: StateFlow<String> = _cameraStatus

    private val _wifiStatus = MutableStateFlow("Checking...")
    val wifiStatus: StateFlow<String> = _wifiStatus

    fun toggleCloudSync() {
        adminController.toggleCloudSync()
    }

    fun checkDeviceStatus() {
        viewModelScope.launch {
            checkWifiStatus()
            checkCameraStatus()
            checkPrinterStatus()
        }
    }

    private fun checkPrinterStatus() {
        val usbManager = getApplication<Application>().getSystemService(Context.USB_SERVICE) as UsbManager
        val deviceList = usbManager.deviceList
        
        // Epson Vendor ID is 0x04B8 (1208 in decimal)
        val epsonPrinter = deviceList.values.find { 
            it.vendorId == 1208 || it.deviceName.contains("Epson", ignoreCase = true)
        }

        if (epsonPrinter != null) {
            _printerStatus.value = "Epson ${epsonPrinter.productName ?: "Printer"} (Connected)"
        } else {
            _printerStatus.value = "Printer Not Found"
        }
    }

    private fun checkWifiStatus() {
        val connectivityManager = getApplication<Application>().getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val network = connectivityManager.activeNetwork
        val capabilities = connectivityManager.getNetworkCapabilities(network)
        
        if (capabilities != null && capabilities.hasTransport(NetworkCapabilities.TRANSPORT_WIFI)) {
            _wifiStatus.value = "Connected to WiFi"
        } else {
            _wifiStatus.value = "Not Connected"
        }
    }

    private fun checkCameraStatus() {
        val context = getApplication<Application>()
        val cameraProviderFuture = ProcessCameraProvider.getInstance(context)
        cameraProviderFuture.addListener({
            try {
                val cameraProvider = cameraProviderFuture.get()
                val hasBackCamera = cameraProvider.hasCamera(androidx.camera.core.CameraSelector.DEFAULT_BACK_CAMERA)
                val hasFrontCamera = cameraProvider.hasCamera(androidx.camera.core.CameraSelector.DEFAULT_FRONT_CAMERA)
                
                if (hasBackCamera || hasFrontCamera) {
                    _cameraStatus.value = "Camera Available"
                } else {
                    _cameraStatus.value = "No Camera Found"
                }
            } catch (e: Exception) {
                _cameraStatus.value = "Error checking camera"
            }
        }, ContextCompat.getMainExecutor(context))
    }
}
