package com.example.myapplication.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import com.example.myapplication.data.local.dao.RecordDao
import com.example.myapplication.data.local.dao.SessionDao
import com.example.myapplication.data.local.entity.RecordEntity
import com.example.myapplication.data.local.entity.SessionEntity

/**
 * 应用数据库
 */
@Database(
    entities = [SessionEntity::class, RecordEntity::class],
    version = 1,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    
    abstract fun sessionDao(): SessionDao
    
    abstract fun recordDao(): RecordDao
    
    companion object {
        const val DATABASE_NAME = "english_reading_db"
    }
}
