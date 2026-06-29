package com.aura.ai.domain.usecase

import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject

class LaunchAppUseCase @Inject constructor(
    @ApplicationContext private val context: Context
) {
    /**
     * Launches an app by [packageName] if provided, or by searching installed apps
     * whose label matches [appName] (case-insensitive).
     */
    fun execute(appName: String, packageName: String? = null): Result<Unit> = runCatching {
        val resolvedPackage = packageName ?: findPackageByName(appName)
            ?: error("App '$appName' not found on this device.")
        val launchIntent = context.packageManager.getLaunchIntentForPackage(resolvedPackage)
            ?: error("Cannot launch '$resolvedPackage'.")
        launchIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        context.startActivity(launchIntent)
    }

    private fun findPackageByName(name: String): String? {
        val pm = context.packageManager
        val apps = pm.getInstalledApplications(PackageManager.GET_META_DATA)
        val normalised = name.trim().lowercase()
        return apps.firstOrNull { app ->
            pm.getApplicationLabel(app).toString().lowercase() == normalised
        }?.packageName
    }
}
