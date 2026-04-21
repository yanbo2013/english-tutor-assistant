package com.example.myapplication.domain.model

/**
 * 文本段落模型
 */
data class TextSegment(
    val index: Int,
    val text: String,
    val audioPath: String? = null,
    val isCompleted: Boolean = false
)
