package com.example.myapplication.data.remote.model

/**
 * 语音识别响应
 */
data class SpeechResponse(
    val success: Boolean,
    val recognizedText: String,
    val confidence: Float,
    val errorMessage: String? = null
)
