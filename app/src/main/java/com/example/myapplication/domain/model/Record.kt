package com.example.myapplication.domain.model

/**
 * 练习记录领域模型
 */
data class Record(
    val id: Long = 0,
    val sessionId: Long,
    val sentenceIndex: Int,
    val originalText: String,
    val studentAudioPath: String?,
    val recognizedText: String?,
    val evaluationResult: EvaluationResult?,
    val createdAt: Long = System.currentTimeMillis()
)
