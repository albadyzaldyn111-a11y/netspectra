package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "firewall_rules")
data class FirewallRule(
    @PrimaryKey val packageName: String,
    val appName: String,
    val isBlocked: Boolean = false,
    val wifiOnly: Boolean = false,
    val mobileOnly: Boolean = false,
    val backgroundBlocked: Boolean = false
)
