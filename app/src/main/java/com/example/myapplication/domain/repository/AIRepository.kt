package com.example.myapplication.domain.repository

import com.example.myapplication.data.remote.model.AIResponse

/**
 * AI 服务仓库接口
 */
interface AIRepository {
    
    /**
     * 理解用户指令，提取要朗读的段落
     */
    suspend fun parseInstruction(
        instruction: String,
        fullText: String,
        paragraphs: List<String>
    ): Result<AIResponse>
    
    /**
     * 生成改进建议
     */
    suspend fun generateSuggestions(
        originalText: String,
        recognizedText: String,
        evaluationScores: Map<String, Float>
    ): Result<AIResponse>
}
