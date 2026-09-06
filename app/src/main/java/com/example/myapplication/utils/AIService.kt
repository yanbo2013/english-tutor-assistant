package com.example.myapplication.utils

import com.example.myapplication.network.ChatMessage
import com.example.myapplication.network.ChatRequest
import com.example.myapplication.network.DeepSeekApi
import com.example.myapplication.network.RetrofitClient

/**
 * AI 对话服务
 *
 * 封装 DeepSeek API 调用，提供统一的 chat() 方法。
 * 当 API Key 未配置（仍是占位符）时，自动降级为本地 mock 回复。
 *
 * 使用方式：
 * ```
 * val reply = AIService.chat("你好，请帮我朗读这篇文章")
 * ```
 */
object AIService {

    /** 占位符 Key，用于检测用户是否真的配置了 API Key */
    private const val PLACEHOLDER_KEY = "your_deepseek_api_key"

    /**
     * 当前是否已配置有效的 API Key
     */
    val isConfigured: Boolean
        get() = Constants.DEEPSEEK_API_KEY.isNotBlank() &&
                Constants.DEEPSEEK_API_KEY != PLACEHOLDER_KEY

    private val api: DeepSeekApi by lazy { RetrofitClient.deepSeekApi }

    /**
     * 发送消息给 AI，获取回复
     *
     * @param userMessage 用户输入的消息文本
     * @param systemPrompt 可选的系统提示词，用于设定 AI 角色
     * @return AI 回复文本；失败时返回本地 mock 回复
     */
    suspend fun chat(
        userMessage: String,
        systemPrompt: String = defaultSystemPrompt
    ): String {
        if (!isConfigured) {
            // Key 未配置，返回本地 mock 回复
            return mockReply(userMessage)
        }

        return try {
            val response = api.chat(
                authorization = "Bearer ${Constants.DEEPSEEK_API_KEY}",
                request = ChatRequest(
                    messages = listOf(
                        ChatMessage(role = "system", content = systemPrompt),
                        ChatMessage(role = "user", content = userMessage)
                    )
                )
            )

            // 优先使用 AI 返回内容
            response.choices?.firstOrNull()?.message?.content
                ?: response.error?.let { "AI 服务返回错误：${it.message}" }
                ?: mockReply(userMessage)
        } catch (e: Exception) {
            // 网络异常、鉴权失败等 → 降级到 mock
            android.util.Log.e("AIService", "DeepSeek API 调用失败，降级为 mock", e)
            mockReply(userMessage)
        }
    }

    /**
     * 默认系统提示词：AI 英语辅导老师角色
     */
    private const val defaultSystemPrompt = """
        你是一位专业的英语辅导老师。你的职责是：
        1. 帮助用户进行英语朗读练习
        2. 纠正发音、语调、语法错误
        3. 提供积极鼓励的反馈
        4. 用简洁明了的中文回复
    """.trimIndent()

    /**
     * 本地 mock 回复
     * 在 API Key 未配置或网络异常时使用，保证功能可用
     */
    private fun mockReply(userMessage: String): String {
        return when {
            userMessage.contains("朗读") || userMessage.contains("读") -> {
                "好的，我来帮您朗读这段内容。请点击下方的播放按钮开始收听。"
            }
            userMessage.contains("练习") || userMessage.contains("学习") -> {
                "好的，让我们开始练习！请准备好后点击开始按钮。"
            }
            userMessage.isBlank() -> {
                "请上传图片或文档，我可以帮您进行英语阅读练习。"
            }
            else -> {
                "收到您的消息：「${userMessage.take(30)}${if (userMessage.length > 30) "..." else ""}」。" +
                "\n\n提示：当前未配置 DeepSeek API Key，使用的是本地模拟回复。" +
                "\n如需接入真实 AI 服务，请在 Constants.kt 中配置 DEEPSEEK_API_KEY。"
            }
        }
    }
}
