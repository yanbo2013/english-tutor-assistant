package com.example.myapplication.data.local.dao

import androidx.room.*
import com.example.myapplication.data.local.entity.RecordEntity
import kotlinx.coroutines.flow.Flow

/**
 * 练习记录数据访问对象
 */
@Dao
interface RecordDao {
    
    @Query("SELECT * FROM records WHERE sessionId = :sessionId ORDER BY sentenceIndex ASC")
    fun getRecordsBySession(sessionId: Long): Flow<List<RecordEntity>>
    
    @Query("SELECT * FROM records WHERE id = :recordId")
    suspend fun getRecordById(recordId: Long): RecordEntity?
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertRecord(record: RecordEntity): Long
    
    @Update
    suspend fun updateRecord(record: RecordEntity)
    
    @Delete
    suspend fun deleteRecord(record: RecordEntity)
    
    @Query("DELETE FROM records WHERE sessionId = :sessionId")
    suspend fun deleteRecordsBySession(sessionId: Long)
}
