package com.aura.ai.androidcontrol.applications

import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import kotlinx.serialization.Serializable
import javax.inject.Inject

@Serializable
data class InstalledApp(
    val name: String,
    val packageName: String,
    val isSystemApp: Boolean
)

class ApplicationControl @Inject constructor(
    private val context: Context
) {
    private val packageManager = context.packageManager

    fun getInstalledApps(): List<InstalledApp> {
        return packageManager.getInstalledApplications(PackageManager.GET_META_DATA)
            .map { appInfo ->
                InstalledApp(
                    name = packageManager.getApplicationLabel(appInfo).toString(),
                    packageName = appInfo.packageName,
                    isSystemApp = (appInfo.flags and android.content.pm.ApplicationInfo.FLAG_SYSTEM) != 0
                )
            }
            .sortedBy { it.name }
    }

    fun getInstalledUserApps(): List<InstalledApp> {
        return getInstalledApps().filter { !it.isSystemApp }
    }

    fun launchApp(packageName: String): Result<String> {
        return try {
            val intent = packageManager.getLaunchIntentForPackage(packageName)
                ?: return Result.failure(Exception("App not found"))
            context.startActivity(intent)
            Result.success("Launched $packageName")
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    fun isAppInstalled(packageName: String): Boolean {
        return try {
            packageManager.getPackageInfo(packageName, 0)
            true
        } catch (e: PackageManager.NameNotFoundException) {
            false
        }
    }

    fun getAppInfo(packageName: String): InstalledApp? {
        return try {
            val appInfo = packageManager.getApplicationInfo(packageName, 0)
            InstalledApp(
                name = packageManager.getApplicationLabel(appInfo).toString(),
                packageName = appInfo.packageName,
                isSystemApp = (appInfo.flags and android.content.pm.ApplicationInfo.FLAG_SYSTEM) != 0
            )
        } catch (e: Exception) {
            null
        }
    }
}
