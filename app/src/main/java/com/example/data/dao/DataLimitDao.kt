package com.example.data.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.data.model.DataLimit
import kotlinx.coroutines.flow.Flow

@Dao
interface DataLimitDao {
    @Query("SELECT * FROM data_limits")
    fun getAllLimitsFlow(): Flow<List<DataLimit>>

    @Query("SELECT * FROM data_limits")
    suspend fun getAllLimits(): List<DataLimit>

    @Query("SELECT * FROM data_limits WHERE limitId = :limitId")
    suspend fun getLimitById(limitId: String): DataLimit?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdateLimit(limit: DataLimit)

    @Delete
    suspend fun deleteLimit(limit: DataLimit)
}
