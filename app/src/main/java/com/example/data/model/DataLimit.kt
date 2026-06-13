package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "data_limits")
data class DataLimit(
    @PrimaryKey val limitId: String, // e.g. "daily", "weekly", "monthly", or custom package identifiers
    val limitBytes: Long,
    val isEnabled: Boolean = false,
    val isBlockedOnOverlimit: Boolean = false
)
