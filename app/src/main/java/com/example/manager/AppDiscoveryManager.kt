package com.example.manager

import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.content.pm.ResolveInfo
import android.os.Build
import android.util.Log
import java.util.Locale

data class InstalledAppInfo(
    val appName: String,
    val packageName: String,
    val activityName: String
)

class AppDiscoveryManager(private val context: Context) {
    private val TAG = "AppDiscoveryManager"

    fun getAllLaunchableApps(): List<InstalledAppInfo> {
        val pm = context.packageManager
        val mainIntent = Intent(Intent.ACTION_MAIN, null).apply {
            addCategory(Intent.CATEGORY_LAUNCHER)
        }

        val resolveInfos: List<ResolveInfo> = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            pm.queryIntentActivities(
                mainIntent,
                PackageManager.ResolveInfoFlags.of(0L)
            )
        } else {
            @Suppress("DEPRECATION")
            pm.queryIntentActivities(mainIntent, 0)
        }

        val appsList = mutableListOf<InstalledAppInfo>()
        for (info in resolveInfos) {
            val label = info.loadLabel(pm).toString().trim()
            val pkg = info.activityInfo.packageName
            val activity = info.activityInfo.name
            if (label.isNotBlank() && pkg.isNotBlank()) {
                appsList.add(InstalledAppInfo(label, pkg, activity))
            }
        }
        return appsList.distinctBy { it.packageName }
    }

    fun findApp(nameQuery: String): InstalledAppInfo? {
        val apps = getAllLaunchableApps()
        val query = nameQuery.trim().lowercase(Locale.ROOT)

        // 1. Exact match
        apps.firstOrNull { it.appName.lowercase(Locale.ROOT) == query }?.let { return it }

        // 2. Starts with query or contains query
        apps.firstOrNull { it.appName.lowercase(Locale.ROOT).startsWith(query) }?.let { return it }
        apps.firstOrNull { it.appName.lowercase(Locale.ROOT).contains(query) }?.let { return it }

        // 3. Known aliases
        val aliasTarget = when (query) {
            "maps", "google maps" -> "maps"
            "yt", "youtube" -> "youtube"
            "browser", "internet", "web" -> "chrome"
            "music", "audio" -> "music"
            "photos", "gallery", "images" -> "photos"
            "calculator", "calc" -> "calculator"
            "camera" -> "camera"
            "clock", "alarm", "timer" -> "clock"
            "settings", "preferences" -> "settings"
            "messages", "sms", "messaging" -> "messages"
            else -> query
        }

        return apps.firstOrNull {
            it.appName.lowercase(Locale.ROOT).contains(aliasTarget) ||
            it.packageName.lowercase(Locale.ROOT).contains(aliasTarget)
        }
    }

    fun getLaunchIntent(app: InstalledAppInfo): Intent? {
        val pm = context.packageManager
        val intent = pm.getLaunchIntentForPackage(app.packageName)
        intent?.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        return intent
    }
}
