package com.example.myapplication.domain.model

/**
 * 学习会话领域模型
 */
data class Session(
    val id: Long = 0,
    val timestamp: Long = System.currentTimeMillis(),
    val imageUrl: String?,
    val recognizedText: String,
    val segments: List<TextSegment>,
    val sessionType: String = "practice",
    val completedAt: Long? = null
)
