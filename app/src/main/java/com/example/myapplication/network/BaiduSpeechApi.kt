package com.example.myapplication.network

import retrofit2.http.*
import okhttp3.RequestBody

/**
 * 百度语音识别 API 接口
 */
interface BaiduSpeechApi {
    
    /**
     * 获取 Access Token
     */
    @GET("oauth/2.0/token")
    suspend fun getAccessToken(
        @Query("grant_type") grantType: String = "client_credentials",
        @Query("client_id") clientId: String,
        @Query("client_secret") clientSecret: String
    ): AccessTokenResponse
    
    /**
     * 短语音识别（REST API - JSON 格式）
     * 注意：使用 JSON 格式，包含 format、rate、channel 等参数
     */
    @POST("server_api")
    suspend fun recognizeSpeech(
        @Query("token") token: String,
        @Query("cuid") cuid: String,  // cuid 也需要放在 URL 参数中
        @Body requestBody: SpeechRecognitionRequest
    ): SpeechRecognitionResponse
}

/**
 * Access Token 响应
 */
data class AccessTokenResponse(
    val access_token: String,
    val expires_in: Int,
    val refresh_token: String?,
    val scope: String?,
    val session_key: String?,
    val session_secret: String?
)

/**
 * 语音识别请求体（JSON 格式）
 */
data class SpeechRecognitionRequest(
    val format: String,      // 音频格式：pcm、wav、amr
    val rate: Int,           // 采样率：8000 或 16000
    val channel: Int,        // 声道数：1（单声道）
    val cuid: String,        // 用户唯一标识
    val dev_pid: Int,        // 语言模型ID
    val speech: String,      // Base64 编码的音频数据
    val len: Int             // 原始音频长度（字节数）
)

/**
 * 语音识别响应
 */
data class SpeechRecognitionResponse(
    val result: List<String>?,
    val err_no: Int,
    val err_msg: String?
)
