package com.example.data.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.data.model.FirewallRule
import kotlinx.coroutines.flow.Flow

@Dao
interface FirewallRuleDao {
    @Query("SELECT * FROM firewall_rules")
    fun getAllRulesFlow(): Flow<List<FirewallRule>>

    @Query("SELECT * FROM firewall_rules")
    suspend fun getAllRules(): List<FirewallRule>

    @Query("SELECT * FROM firewall_rules WHERE packageName = :packageName")
    suspend fun getRuleForPackage(packageName: String): FirewallRule?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdateRule(rule: FirewallRule)

    @Delete
    suspend fun deleteRule(rule: FirewallRule)

    @Query("DELETE FROM firewall_rules WHERE packageName = :packageName")
    suspend fun deleteRuleForPackage(packageName: String)
}
