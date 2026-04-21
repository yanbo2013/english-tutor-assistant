package com.example.myapplication.data.remote.model

/**
 * AI 响应（指令理解、改进建议）
 */
data class AIResponse(
    val success: Boolean,
    val selectedSegments: List<Int>?, // 选中的段落索引
    val suggestions: String?, // 改进建议
    val errorMessage: String? = null
)
