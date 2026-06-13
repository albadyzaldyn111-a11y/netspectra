package com.example.data.network

import android.app.usage.NetworkStatsManager
import android.content.Context
import android.net.ConnectivityManager
import android.net.TrafficStats
import android.os.Build
import android.util.Log
import java.util.Calendar

data class NetworkUsage(
    val rxBytes: Long,
    val txBytes: Long,
    val totalBytes: Long = rxBytes + txBytes
)

class NetworkStatsHelper(private val context: Context) {
    private val networkStatsManager = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
        context.getSystemService(Context.NETWORK_STATS_SERVICE) as NetworkStatsManager
    } else null

    fun getStartOfToday(): Long {
        val calendar = Calendar.getInstance()
        calendar.set(Calendar.HOUR_OF_DAY, 0)
        calendar.set(Calendar.MINUTE, 0)
        calendar.set(Calendar.SECOND, 0)
        calendar.set(Calendar.MILLISECOND, 0)
        return calendar.timeInMillis
    }

    fun getStartOfWeek(): Long {
        val calendar = Calendar.getInstance()
        // Standardize Sunday/Monday depending on config
        calendar.set(Calendar.DAY_OF_WEEK, calendar.firstDayOfWeek)
        calendar.set(Calendar.HOUR_OF_DAY, 0)
        calendar.set(Calendar.MINUTE, 0)
        calendar.set(Calendar.SECOND, 0)
        calendar.set(Calendar.MILLISECOND, 0)
        return calendar.timeInMillis
    }

    fun getStartOfMonth(): Long {
        val calendar = Calendar.getInstance()
        calendar.set(Calendar.DAY_OF_MONTH, 1)
        calendar.set(Calendar.HOUR_OF_DAY, 0)
        calendar.set(Calendar.MINUTE, 0)
        calendar.set(Calendar.SECOND, 0)
        calendar.set(Calendar.MILLISECOND, 0)
        return calendar.timeInMillis
    }

    /**
     * Fetch global speeds and totals
     */
    fun getGlobalUsage(startTime: Long, endTime: Long): NetworkUsage {
        var wifiRx = 0L
        var wifiTx = 0L
        var mobileRx = 0L
        var mobileTx = 0L

        if (networkStatsManager == null) {
            // Fallback to TrafficStats totals if API is unavailable (though it won't have histories)
            return NetworkUsage(TrafficStats.getTotalRxBytes(), TrafficStats.getTotalTxBytes())
        }

        try {
            val wifiBucket = networkStatsManager.querySummaryForDevice(ConnectivityManager.TYPE_WIFI, null, startTime, endTime)
            wifiRx = wifiBucket.rxBytes
            wifiTx = wifiBucket.txBytes
        } catch (e: Exception) {
            // Suppress if wifi is disabled or permission pending
        }

        try {
            val mobileBucket = networkStatsManager.querySummaryForDevice(ConnectivityManager.TYPE_MOBILE, null, startTime, endTime)
            mobileRx = mobileBucket.rxBytes
            mobileTx = mobileBucket.txBytes
        } catch (e: Exception) {
            // Suppress cellular-less devices
        }

        return NetworkUsage(wifiRx + mobileRx, wifiTx + mobileTx)
    }

    /**
     * Query usage summaries for all applications across a time slot.
     */
    fun getAllAppsUsage(startTime: Long, endTime: Long): Map<Int, NetworkUsage> {
        val usages = mutableMapOf<Int, NetworkUsage>()
        if (networkStatsManager == null) return usages

        // 1. Mobile data mapping
        try {
            val stats = networkStatsManager.querySummary(ConnectivityManager.TYPE_MOBILE, null, startTime, endTime)
            val bucket = android.app.usage.NetworkStats.Bucket()
            while (stats.hasNextBucket()) {
                stats.getNextBucket(bucket)
                val uid = bucket.uid
                val rx = bucket.rxBytes
                val tx = bucket.txBytes
                val existing = usages[uid] ?: NetworkUsage(0, 0)
                usages[uid] = NetworkUsage(existing.rxBytes + rx, existing.txBytes + tx)
            }
            stats.close()
        } catch (e: Exception) {
            // Mobile stats capture omitted
        }

        // 2. WiFi data mapping
        try {
            val stats = networkStatsManager.querySummary(ConnectivityManager.TYPE_WIFI, null, startTime, endTime)
            val bucket = android.app.usage.NetworkStats.Bucket()
            while (stats.hasNextBucket()) {
                stats.getNextBucket(bucket)
                val uid = bucket.uid
                val rx = bucket.rxBytes
                val tx = bucket.txBytes
                val existing = usages[uid] ?: NetworkUsage(0, 0)
                usages[uid] = NetworkUsage(existing.rxBytes + rx, existing.txBytes + tx)
            }
            stats.close()
        } catch (e: Exception) {
            // WiFi stats capture omitted
        }

        return usages
    }

    /**
     * Query stats specifically for a single app's UID
     */
    fun getAppUsageForUid(uid: Int, startTime: Long, endTime: Long): NetworkUsage {
        var rxValue = 0L
        var txValue = 0L
        if (networkStatsManager == null) {
            return NetworkUsage(TrafficStats.getUidRxBytes(uid), TrafficStats.getUidTxBytes(uid))
        }

        // Gather Mobile
        try {
            val stats = networkStatsManager.queryDetailsForUid(ConnectivityManager.TYPE_MOBILE, null, startTime, endTime, uid)
            val bucket = android.app.usage.NetworkStats.Bucket()
            while (stats.hasNextBucket()) {
                stats.getNextBucket(bucket)
                rxValue += bucket.rxBytes
                txValue += bucket.txBytes
            }
            stats.close()
        } catch (e: Exception) {
            // Ignore
        }

        // Gather WiFi
        try {
            val stats = networkStatsManager.queryDetailsForUid(ConnectivityManager.TYPE_WIFI, null, startTime, endTime, uid)
            val bucket = android.app.usage.NetworkStats.Bucket()
            while (stats.hasNextBucket()) {
                stats.getNextBucket(bucket)
                rxValue += bucket.rxBytes
                txValue += bucket.txBytes
            }
            stats.close()
        } catch (e: Exception) {
            // Ignore
        }

        return NetworkUsage(rxValue, txValue)
    }
}
