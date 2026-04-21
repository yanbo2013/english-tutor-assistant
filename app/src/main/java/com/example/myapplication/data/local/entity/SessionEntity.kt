package com.example.myapplication.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * 学习会话实体类
 */
@Entity(tableName = "sessions")
data class SessionEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val timestamp: Long = System.currentTimeMillis(),
    val imageUrl: String?,
    val recognizedText: String,
    val selectedSegments: String, // JSON 字符串，存储选中的段落
    val sessionType: String = "practice", // practice, review
    val completedAt: Long? = null
)
