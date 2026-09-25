package com.aura.ai.core.permissions

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.content.ContextCompat
import javax.inject.Inject

class PermissionManager @Inject constructor(
    private val context: Context
) {
    fun hasPermission(permission: String): Boolean {
        return ContextCompat.checkSelfPermission(context, permission) == PackageManager.PERMISSION_GRANTED
    }

    fun hasAllPermissions(permissions: List<String>): Boolean {
        return permissions.all { hasPermission(it) }
    }

    fun getMissingPermissions(permissions: List<String>): List<String> {
        return permissions.filter { !hasPermission(it) }
    }

    // Voice-related permissions
    fun hasRecordAudioPermission(): Boolean = hasPermission(Manifest.permission.RECORD_AUDIO)
    fun hasPostNotificationPermission(): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            hasPermission(Manifest.permission.POST_NOTIFICATIONS)
        } else {
            true
        }
    }

    // Contact and calendar permissions
    fun hasReadContactsPermission(): Boolean = hasPermission(Manifest.permission.READ_CONTACTS)
    fun hasWriteContactsPermission(): Boolean = hasPermission(Manifest.permission.WRITE_CONTACTS)
    fun hasReadCalendarPermission(): Boolean = hasPermission(Manifest.permission.READ_CALENDAR)
    fun hasWriteCalendarPermission(): Boolean = hasPermission(Manifest.permission.WRITE_CALENDAR)

    // SMS and call permissions
    fun hasReadSmsPermission(): Boolean = hasPermission(Manifest.permission.READ_SMS)
    fun hasSendSmsPermission(): Boolean = hasPermission(Manifest.permission.SEND_SMS)
    fun hasCallPhonePermission(): Boolean = hasPermission(Manifest.permission.CALL_PHONE)
    fun hasReadPhoneStatePermission(): Boolean = hasPermission(Manifest.permission.READ_PHONE_STATE)

    // File and media permissions
    fun hasReadExternalStoragePermission(): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            hasPermission(Manifest.permission.READ_MEDIA_IMAGES) ||
            hasPermission(Manifest.permission.READ_MEDIA_VIDEO) ||
            hasPermission(Manifest.permission.READ_MEDIA_AUDIO)
        } else {
            hasPermission(Manifest.permission.READ_EXTERNAL_STORAGE)
        }
    }

    fun hasWriteExternalStoragePermission(): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            hasPermission(Manifest.permission.MANAGE_EXTERNAL_STORAGE) ||
            hasPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE)
        } else {
            hasPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE)
        }
    }

    // Camera permission
    fun hasCameraPermission(): Boolean = hasPermission(Manifest.permission.CAMERA)

    // Location permissions
    fun hasAccessFineLocationPermission(): Boolean = hasPermission(Manifest.permission.ACCESS_FINE_LOCATION)
    fun hasAccessCoarseLocationPermission(): Boolean = hasPermission(Manifest.permission.ACCESS_COARSE_LOCATION)

    // Internet permission (always granted if in manifest)
    fun hasInternetPermission(): Boolean = true
}
