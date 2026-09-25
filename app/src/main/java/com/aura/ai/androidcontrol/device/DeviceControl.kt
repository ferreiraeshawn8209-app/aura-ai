package com.aura.ai.androidcontrol.device

import android.content.Context
import android.os.Build
import android.provider.Settings
import kotlinx.serialization.Serializable
import javax.inject.Inject

@Serializable
data class DeviceInfo(
    val brand: String,
    val model: String,
    val osVersion: Int,
    val osVersionRelease: String,
    val deviceName: String,
    val androidId: String,
    val manufacturer: String
)

class DeviceControl @Inject constructor(
    private val context: Context
) {
    fun getDeviceInfo(): DeviceInfo {
        return DeviceInfo(
            brand = Build.BRAND,
            model = Build.MODEL,
            osVersion = Build.VERSION.SDK_INT,
            osVersionRelease = Build.VERSION.RELEASE,
            deviceName = Build.DEVICE,
            androidId = Settings.Secure.getString(context.contentResolver, Settings.Secure.ANDROID_ID),
            manufacturer = Build.MANUFACTURER
        )
    }

    fun getDeviceName(): String {
        return Settings.Secure.getString(context.contentResolver, "bluetooth_name") ?: "Unknown Device"
    }

    fun getBatteryPercentage(): Int? {
        return try {
            val batteryManager = context.getSystemService(Context.BATTERY_SERVICE) as android.os.BatteryManager
            batteryManager.getIntProperty(android.os.BatteryManager.BATTERY_PROPERTY_CAPACITY)
        } catch (e: Exception) {
            null
        }
    }
}
