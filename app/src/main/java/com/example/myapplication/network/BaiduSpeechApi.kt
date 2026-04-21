package com.example.myapplication.network

import retrofit2.http.*
import okhttp3.RequestBody
import okhttp3.ResponseBody

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
     * 短语音识别（REST API）
     */
    @POST("v1/pro_api")
    @Headers("Content-Type: audio/pcm;rate=16000")
    suspend fun recognizeSpeech(
        @Url url: String,
        @Body audioData: RequestBody
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
 * 语音识别响应
 */
data class SpeechRecognitionResponse(
    val result: List<String>?,
    val err_no: Int,
    val err_msg: String?
)
