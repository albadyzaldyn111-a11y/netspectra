package com.example.data.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.example.data.dao.DataLimitDao
import com.example.data.dao.FirewallRuleDao
import com.example.data.model.DataLimit
import com.example.data.model.FirewallRule

@Database(entities = [FirewallRule::class, DataLimit::class], version = 1, exportSchema = false)
abstract class NetSpectraDatabase : RoomDatabase() {
    abstract fun firewallRuleDao(): FirewallRuleDao
    abstract fun dataLimitDao(): DataLimitDao

    companion object {
        @Volatile
        private var INSTANCE: NetSpectraDatabase? = null

        fun getDatabase(context: Context): NetSpectraDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    NetSpectraDatabase::class.java,
                    "netspectra_database"
                )
                .fallbackToDestructiveMigration()
                .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
