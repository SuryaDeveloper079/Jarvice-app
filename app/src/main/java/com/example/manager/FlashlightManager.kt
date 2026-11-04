package com.example.manager

import android.content.Context
import android.hardware.camera2.CameraCharacteristics
import android.hardware.camera2.CameraManager
import android.util.Log
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class FlashlightManager(private val context: Context) {
    private val TAG = "FlashlightManager"
    private val cameraManager = context.getSystemService(Context.CAMERA_SERVICE) as? CameraManager
    private var cameraId: String? = null

    private val _isTorchOn = MutableStateFlow(false)
    val isTorchOn: StateFlow<Boolean> = _isTorchOn.asStateFlow()

    init {
        try {
            cameraManager?.cameraIdList?.firstOrNull { id ->
                val chars = cameraManager.getCameraCharacteristics(id)
                chars.get(CameraCharacteristics.FLASH_INFO_AVAILABLE) == true
            }?.let { id ->
                cameraId = id
            }

            cameraManager?.registerTorchCallback(object : CameraManager.TorchCallback() {
                override fun onTorchModeChanged(camId: String, enabled: Boolean) {
                    if (camId == cameraId) {
                        _isTorchOn.value = enabled
                    }
                }
            }, null)
        } catch (e: Exception) {
            Log.e(TAG, "Error initializing FlashlightManager", e)
        }
    }

    fun setTorch(enable: Boolean): Boolean {
        val id = cameraId ?: return false
        return try {
            cameraManager?.setTorchMode(id, enable)
            _isTorchOn.value = enable
            true
        } catch (e: Exception) {
            Log.e(TAG, "Failed to set torch mode to $enable", e)
            false
        }
    }

    fun toggleTorch(): Boolean {
        return setTorch(!_isTorchOn.value)
    }

    fun hasFlashlight(): Boolean = cameraId != null
}
