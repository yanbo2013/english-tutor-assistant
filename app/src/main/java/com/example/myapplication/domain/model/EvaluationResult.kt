package com.example.myapplication.domain.model

/**
 * 发音评测结果
 */
data class EvaluationResult(
    val accuracyScore: Float, // 准确度 0-100
    val fluencyScore: Float, // 流利度 0-100
    val completenessScore: Float, // 完整度 0-100
    val overallScore: Float, // 综合评分
    val feedback: String, // 改进建议
    val wordDetails: List<WordEvaluation>? = null
)

/**
 * 单词级别评测详情
 */
data class WordEvaluation(
    val word: String,
    val accuracyScore: Float,
    val errorType: String? = null // "mispronunciation", "omission", "insertion"
)
