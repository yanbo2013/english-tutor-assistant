package com.example.myapplication.data.local.dao

import androidx.room.*
import com.example.myapplication.data.local.entity.SessionEntity
import kotlinx.coroutines.flow.Flow

/**
 * 学习会话数据访问对象
 */
@Dao
interface SessionDao {
    
    @Query("SELECT * FROM sessions ORDER BY timestamp DESC")
    fun getAllSessions(): Flow<List<SessionEntity>>
    
    @Query("SELECT * FROM sessions WHERE id = :sessionId")
    suspend fun getSessionById(sessionId: Long): SessionEntity?
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSession(session: SessionEntity): Long
    
    @Update
    suspend fun updateSession(session: SessionEntity)
    
    @Delete
    suspend fun deleteSession(session: SessionEntity)
    
    @Query("DELETE FROM sessions")
    suspend fun deleteAllSessions()
}
