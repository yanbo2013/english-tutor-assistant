package com.example.myapplication.domain.repository

import com.example.myapplication.data.remote.model.TTSResponse

/**
 * TTS 语音合成仓库接口
 */
interface TTSRepository {
    
    /**
     * 生成单句音频
     */
    suspend fun generateAudio(text: String): Result<TTSResponse>
    
    /**
     * 批量生成音频
     */
    suspend fun generateBatchAudio(texts: List<String>): Result<List<TTSResponse>>
}
