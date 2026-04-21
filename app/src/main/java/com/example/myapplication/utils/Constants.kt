package com.example.myapplication.utils

/**
 * 应用常量
 */
object Constants {
    
    // API Keys (需要从服务商获取)
    const val XUNFEI_APP_ID = "your_xunfei_app_id"
    const val XUNFEI_API_KEY = "your_xunfei_api_key"
    const val XUNFEI_API_SECRET = "your_xunfei_api_secret"
    
    const val DEEPSEEK_API_KEY = "your_deepseek_api_key"
    
    // API URLs
    const val XUNFEI_TTS_URL = "https://api.xfyun.cn/v1/service/v1/tts"
    const val XUNFEI_STT_URL = "https://api.xfyun.cn/v1/service/v1/iat"
    const val XUNFEI_EVAL_URL = "https://api.xfyun.cn/v1/service/v1/ise"
    const val DEEPSEEK_API_URL = "https://api.deepseek.com/v1/chat/completions"
    
    // 文件路径
    const val AUDIO_DIR = "audio_records"
    const val IMAGE_DIR = "captured_images"
    
    // 默认配置
    const val DEFAULT_LANGUAGE = "en_us" // 英语
    const val MAX_RECORD_DURATION = 60000L // 最大录音时长 60秒
    const val AUDIO_SAMPLE_RATE = 16000 // 音频采样率
}
