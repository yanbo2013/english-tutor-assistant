package com.example.myapplication.network

import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

/**
 * Retrofit 客户端单例
 */
object RetrofitClient {
    
    private const val BAIDU_SPEECH_BASE_URL = "https://vop.baidu.com/"
    private const val BAIDU_OAUTH_BASE_URL = "https://aip.baidubce.com/"
    private const val DEEPSEEK_BASE_URL = "https://api.deepseek.com/v1/"
    
    private val loggingInterceptor = HttpLoggingInterceptor().apply {
        level = HttpLoggingInterceptor.Level.BODY
    }
    
    private val okHttpClient = OkHttpClient.Builder()
        .addInterceptor(loggingInterceptor)
        .connectTimeout(30, TimeUnit.SECONDS)
        .readTimeout(30, TimeUnit.SECONDS)
        .writeTimeout(30, TimeUnit.SECONDS)
        .build()
    
    /**
     * 百度语音识别 API 客户端
     */
    val baiduSpeechApi: BaiduSpeechApi by lazy {
        Retrofit.Builder()
            .baseUrl(BAIDU_SPEECH_BASE_URL)
            .client(okHttpClient)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(BaiduSpeechApi::class.java)
    }
    
    /**
     * 百度 OAuth API 客户端（用于获取 Token）
     */
    val baiduOAuthApi: BaiduSpeechApi by lazy {
        Retrofit.Builder()
            .baseUrl(BAIDU_OAUTH_BASE_URL)
            .client(okHttpClient)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(BaiduSpeechApi::class.java)
    }

    /**
     * DeepSeek 大模型 API 客户端
     */
    val deepSeekApi: DeepSeekApi by lazy {
        Retrofit.Builder()
            .baseUrl(DEEPSEEK_BASE_URL)
            .client(okHttpClient)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(DeepSeekApi::class.java)
    }
}
