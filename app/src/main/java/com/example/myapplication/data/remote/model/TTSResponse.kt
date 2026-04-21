package com.example.myapplication.data.remote.model

/**
 * TTS 语音合成响应
 */
data class TTSResponse(
    val success: Boolean,
    val audioUrl: String?,
    val audioPath: String?, // 本地文件路径
    val duration: Long, // 音频时长（毫秒）
    val errorMessage: String? = null
)
