package com.example.ui.state

import com.example.data.model.DataLimit
import com.example.data.model.FirewallRule
import com.example.data.network.NetworkUsage
import com.example.data.packageinfo.AppItem

data class DetailedAppUsage(
    val app: AppItem,
    val usageToday: NetworkUsage,
    val usageWeek: NetworkUsage,
    val usageMonth: NetworkUsage,
    val activeRule: FirewallRule? = null,
    val downloadSpeed: Long = 0L,
    val uploadSpeed: Long = 0L
)

data class NetUiState(
    // Global metric aggregates
    val todayTotalUsage: NetworkUsage = NetworkUsage(0, 0),
    val weekTotalUsage: NetworkUsage = NetworkUsage(0, 0),
    val monthTotalUsage: NetworkUsage = NetworkUsage(0, 0),
    
    // Limits
    val limits: List<DataLimit> = emptyList(),
    val overlimitNotifications: List<String> = emptyList(),

    // Network stats
    val uploadSpeedBps: Long = 0,
    val downloadSpeedBps: Long = 0,
    val connectedAppsCount: Int = 0,
    val activeConnectionsCount: Int = 0,
    val blockedAppsCount: Int = 0,
    val alertCount: Int = 0,
    val currentIp: String = "--",
    val primaryDns: String = "--",
    val connectionType: String = "--",
    val pingMs: Long = 0L,

    // App usage profiles
    val appUsageList: List<DetailedAppUsage> = emptyList(),
    val allInstalledApps: List<AppItem> = emptyList(),
    val activeFirewallRules: List<FirewallRule> = emptyList(),

    // Search and filters
    val searchQuery: String = "",
    val activeFilter: FilterType = FilterType.ALL,
    val activeSort: SortType = SortType.CONSUMPTION,
    val vpnActive: Boolean = false,

    // Loading states
    val isLoading: Boolean = true
)

enum class FilterType {
    ALL,
    USER,
    SYSTEM,
    BLOCKED
}

enum class SortType {
    NAME,
    CONSUMPTION,
    INSTALL_DATE,
    UID
}
