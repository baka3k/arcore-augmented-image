package com.baka3k.test.bankcardaugument.utils

import android.hardware.camera2.CameraCharacteristics
import android.hardware.camera2.CameraManager
import android.util.SparseIntArray
import android.view.Surface
import android.view.WindowManager

class CameraInfo(
    private val windowManager: WindowManager,
    private val cameraManager: CameraManager
) {
     fun getRotation(): Int {
        val deviceRotation =
            windowManager.defaultDisplay.rotation //windowManager.defaultDisplay.rotation
        var rotationCompensation = ORIENTATIONS.get(deviceRotation)
        val sensorOrientation = cameraManager
            .getCameraCharacteristics(
                getCameraId(
                    cameraManager,
                    CameraCharacteristics.LENS_FACING_BACK
                )
            )
            .get(CameraCharacteristics.SENSOR_ORIENTATION)!!
        rotationCompensation = (rotationCompensation + sensorOrientation + 270) % 360
        return rotationCompensation
    }

    private fun getCameraId(manager: CameraManager, facing: Int): String {
        return manager.cameraIdList.first {
            manager
                .getCameraCharacteristics(it)
                .get(CameraCharacteristics.LENS_FACING) == facing
        }
    }

    private val ORIENTATIONS = SparseIntArray()

    init {
        ORIENTATIONS.append(Surface.ROTATION_0, 90)
        ORIENTATIONS.append(Surface.ROTATION_90, 0)
        ORIENTATIONS.append(Surface.ROTATION_180, 270)
        ORIENTATIONS.append(Surface.ROTATION_270, 180)
    }
}