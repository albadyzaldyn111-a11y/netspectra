package com.example.data.repository

import com.example.data.dao.DataLimitDao
import com.example.data.dao.FirewallRuleDao
import com.example.data.model.DataLimit
import com.example.data.model.FirewallRule
import kotlinx.coroutines.flow.Flow

class NetSpectraRepository(
    private val firewallRuleDao: FirewallRuleDao,
    private val dataLimitDao: DataLimitDao
) {
    val allRulesFlow: Flow<List<FirewallRule>> = firewallRuleDao.getAllRulesFlow()
    val allLimitsFlow: Flow<List<DataLimit>> = dataLimitDao.getAllLimitsFlow()

    suspend fun getAllRules(): List<FirewallRule> = firewallRuleDao.getAllRules()
    suspend fun getRuleForPackage(packageName: String): FirewallRule? = firewallRuleDao.getRuleForPackage(packageName)
    suspend fun insertOrUpdateRule(rule: FirewallRule) = firewallRuleDao.insertOrUpdateRule(rule)
    suspend fun deleteRule(rule: FirewallRule) = firewallRuleDao.deleteRule(rule)
    suspend fun deleteRuleForPackage(packageName: String) = firewallRuleDao.deleteRuleForPackage(packageName)

    suspend fun getAllLimits(): List<DataLimit> = dataLimitDao.getAllLimits()
    suspend fun getLimitById(limitId: String): DataLimit? = dataLimitDao.getLimitById(limitId)
    suspend fun insertOrUpdateLimit(limit: DataLimit) = dataLimitDao.insertOrUpdateLimit(limit)
    suspend fun deleteLimit(limit: DataLimit) = dataLimitDao.deleteLimit(limit)
}
