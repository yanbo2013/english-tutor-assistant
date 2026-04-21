package com.example.myapplication.domain.repository

import com.example.myapplication.data.remote.model.SpeechResponse

/**
 * 语音识别仓库接口
 */
interface SpeechRepository {
    
    /**
     * 识别录音文件
     */
    suspend fun recognizeSpeech(audioPath: String): Result<SpeechResponse>
    
    /**
     * 开始实时录音识别
     */
    fun startRealtimeRecognition(onResult: (String) -> Unit)
    
    /**
     * 停止实时录音识别
     */
    fun stopRealtimeRecognition()
}
