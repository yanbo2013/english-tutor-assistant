package com.example.myapplication.data.remote.model

/**
 * OCR 识别响应
 */
data class OCRResponse(
    val success: Boolean,
    val text: String,
    val paragraphs: List<String>,
    val confidence: Float,
    val errorMessage: String? = null
)
