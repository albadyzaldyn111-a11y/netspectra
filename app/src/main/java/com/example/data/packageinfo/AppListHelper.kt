package com.example.data.packageinfo

import android.app.usage.UsageStatsManager
import android.content.Context
import android.content.pm.ApplicationInfo
import android.content.pm.PackageManager
import android.graphics.drawable.Drawable

data class AppItem(
    val packageName: String,
    val appName: String,
    val uid: Int,
    val versionName: String,
    val installTime: Long,
    val isSystem: Boolean,
    val lastActive: Long = 0L,
    val icon: Drawable? = null
)

class AppListHelper(private val context: Context) {
    fun getInstalledApps(): List<AppItem> {
        val pm = context.packageManager
        val appsList = mutableListOf<AppItem>()
        
        // Fetch last active times via UsageStatsManager
        val usageStatsManager = context.getSystemService(Context.USAGE_STATS_SERVICE) as? UsageStatsManager
        val endTime = System.currentTimeMillis()
        val startTime = endTime - 30L * 24 * 60 * 60 * 1000L // last 30 days
        val usageStats = try {
            usageStatsManager?.queryAndAggregateUsageStats(startTime, endTime) ?: emptyMap()
        } catch (e: Exception) {
            emptyMap()
        }
        
        try {
            val packages = pm.getInstalledPackages(0)
            for (pkg in packages) {
                val appInfo = pkg.applicationInfo ?: continue
                
                // Fetch localized label
                val appName = pm.getApplicationLabel(appInfo).toString()
                
                // Differentiate system apps
                val isSystem = (appInfo.flags and ApplicationInfo.FLAG_SYSTEM) != 0 || 
                               (appInfo.flags and ApplicationInfo.FLAG_UPDATED_SYSTEM_APP) != 0

                val icon = try {
                    pm.getApplicationIcon(appInfo)
                } catch (e: Exception) {
                    null
                }

                val lastActiveTime = try {
                    usageStats[pkg.packageName]?.lastTimeUsed ?: 0L
                } catch (e: Exception) {
                    0L
                }

                appsList.add(
                    AppItem(
                        packageName = pkg.packageName,
                        appName = appName,
                        uid = appInfo.uid,
                        versionName = pkg.versionName ?: "1.0",
                        installTime = pkg.firstInstallTime,
                        isSystem = isSystem,
                        lastActive = lastActiveTime,
                        icon = icon
                    )
                )
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }

        return appsList.sortedBy { it.appName.lowercase() }
    }
}

