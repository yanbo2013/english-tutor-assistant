package com.example.myapplication.domain.repository

import com.example.myapplication.domain.model.EvaluationResult

/**
 * 发音评测仓库接口
 */
interface EvaluationRepository {
    
    /**
     * 评测学生发音
     */
    suspend fun evaluateSpeech(
        referenceText: String,
        audioPath: String
    ): Result<EvaluationResult>
}
