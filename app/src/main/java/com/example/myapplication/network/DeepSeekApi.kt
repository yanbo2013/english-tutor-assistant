package com.example.myapplication.network

import retrofit2.http.*

/**
 * DeepSeek 大模型 API 接口
 *
 * 文档：https://platform.deepseek.com/api-docs/
 * 端点：POST https://api.deepseek.com/v1/chat/completions
 */
interface DeepSeekApi {

    @POST("chat/completions")
    suspend fun chat(
        @Header("Authorization") authorization: String,
        @Body request: ChatRequest
    ): ChatResponse
}

/**
 * 聊天请求体
 */
data class ChatRequest(
    val model: String = "deepseek-chat",
    val messages: List<ChatMessage>,
    val temperature: Float = 0.7f,
    val max_tokens: Int = 1024
)

/**
 * 聊天消息
 */
data class ChatMessage(
    val role: String, // "system", "user", "assistant"
    val content: String
)

/**
 * 聊天响应
 */
data class ChatResponse(
    val id: String?,
    val choices: List<Choice>?,
    val usage: Usage?,
    val error: ApiError?
)

data class Choice(
    val index: Int,
    val message: ChatMessage,
    val finish_reason: String?
)

data class Usage(
    val prompt_tokens: Int,
    val completion_tokens: Int,
    val total_tokens: Int
)

data class ApiError(
    val message: String,
    val type: String?,
    val code: String?
)
