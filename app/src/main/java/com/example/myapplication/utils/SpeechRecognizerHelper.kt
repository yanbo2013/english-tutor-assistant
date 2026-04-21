package com.example.myapplication.utils

import android.content.Context
import android.media.MediaRecorder
import android.os.Build
import android.util.Log
import com.example.myapplication.BuildConfig
import com.example.myapplication.network.RetrofitClient
import kotlinx.coroutines.*
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.RequestBody
import java.io.File

/**
 * 百度语音识别助手
 * 
 * 功能：
 * 1. 录音并保存为 AMR 格式
 * 2. 调用百度语音识别 API 将音频转换为文字
 * 3. 支持中英文混合识别
 * 
 * 使用步骤：
 * 1. 调用 startRecording() 开始录音
 * 2. 调用 stopRecordingAndRecognize() 停止录音并开始识别
 * 3. 在 onResult 回调中获取识别结果
 * 4. 使用完毕后调用 destroy() 释放资源
 */
class SpeechRecognizerHelper(
    private val context: Context,
    private val onResult: (String) -> Unit,
    private val onError: (String) -> Unit,
    private val onRecordingStateChanged: (Boolean) -> Unit
) {
    private var mediaRecorder: MediaRecorder? = null
    // 临时音频文件（MediaRecorder 限制，必须使用文件）
    private var _tempAudioFile: File? = null
    private var isRecording = false
    private var accessToken: String? = null
    private var tokenExpireTime: Long = 0
    
    // 设备唯一标识（CUID）- 使用工具类生成，添加语音识别前缀
    private val deviceId: String by lazy {
        DeviceIdGenerator.getDeviceId(context, prefix = "baidu_speech")
    }
    
    private val scope = CoroutineScope(Dispatchers.IO + SupervisorJob())
    
    companion object {
        private const val TAG = "BaiduSpeechRecognizer"
        private const val SAMPLE_RATE = 16000 // 采样率
        
        // 百度语音识别 API 基础配置
        private const val BAIDU_SPEECH_API_BASE_URL = "https://vop.baidu.com/server_api"
        private const val DEV_PID = 80001 // 普通话+英语混合识别
        private const val AUDIO_FORMAT = "audio/amr" // 音频格式
    }
    
    /**
     * 开始录音
     */
    fun startRecording() {
        try {
            // 创建临时音频文件
            // 注意：Android MediaRecorder API 限制，必须输出到文件，无法直接输出到内存
            _tempAudioFile = File.createTempFile("speech_", ".amr", context.cacheDir)
            
            mediaRecorder = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                MediaRecorder(context)
            } else {
                @Suppress("DEPRECATION")
                MediaRecorder()
            }.apply {
                setAudioSource(MediaRecorder.AudioSource.MIC)
                setOutputFormat(MediaRecorder.OutputFormat.AMR_NB)
                setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB)
                setAudioSamplingRate(SAMPLE_RATE)
                setOutputFile(_tempAudioFile?.absolutePath)
                prepare()
                start()
            }
            
            isRecording = true
            onRecordingStateChanged(true)
            Log.d(TAG, "开始录音")
            
        } catch (e: Exception) {
            e.printStackTrace()
            onError("录音失败: ${e.message}")
            isRecording = false
            onRecordingStateChanged(false)
        }
    }
    
    /**
     * 停止录音并开始识别
     */
    fun stopRecordingAndRecognize() {
        if (!isRecording) return
        
        try {
            mediaRecorder?.apply {
                stop()
                release()
            }
            mediaRecorder = null
            isRecording = false
            onRecordingStateChanged(false)
            
            Log.d(TAG, "停止录音，开始识别")
            
            // 异步调用百度 API 进行识别
            scope.launch {
                recognizeWithBaiduAPI()
            }
            
        } catch (e: Exception) {
            e.printStackTrace()
            onError("停止录音失败: ${e.message}")
            isRecording = false
            onRecordingStateChanged(false)
        }
    }
    
    /**
     * 调用百度语音识别 API
     */
    private suspend fun recognizeWithBaiduAPI() {
        try {
            // 1. 获取 Access Token
            val token = getAccessToken()
            if (token == null) {
                withContext(Dispatchers.Main) {
                    onError("获取 Token 失败")
                }
                return
            }
            
            // 2. 读取音频数据（从临时文件）
            val audioData = _tempAudioFile?.readBytes()
            if (audioData == null || audioData.isEmpty()) {
                withContext(Dispatchers.Main) {
                    onError("音频数据为空")
                }
                return
            }
            
            Log.d(TAG, "音频数据大小: ${audioData.size} bytes")
            
            // 3. 调用识别 API（常量配置 + 动态拼接）
            val url = buildString {
                append(BAIDU_SPEECH_API_BASE_URL)
                append("?dev_pid=").append(DEV_PID)
                append("&cuid=").append(deviceId)
                append("&token=").append(token)
            }
            val mediaType = AUDIO_FORMAT.toMediaTypeOrNull()
                ?: throw IllegalStateException("Invalid media type: $AUDIO_FORMAT")
            val requestBody = RequestBody.create(mediaType, audioData)
            
            val response = RetrofitClient.baiduSpeechApi.recognizeSpeech(url, requestBody)
            
            // 4. 处理结果
            withContext(Dispatchers.Main) {
                if (response.err_no == 0 && response.result != null && response.result.isNotEmpty()) {
                    val recognizedText = response.result.joinToString("")
                    Log.d(TAG, "识别成功: $recognizedText")
                    onResult(recognizedText)
                } else {
                    val errorMsg = response.err_msg ?: "未知错误"
                    Log.e(TAG, "识别失败: err_no=${response.err_no}, msg=$errorMsg")
                    onError("识别失败: $errorMsg")
                }
            }
            
        } catch (e: Exception) {
            e.printStackTrace()
            withContext(Dispatchers.Main) {
                onError("网络请求失败: ${e.message}")
            }
        } finally {
            // 清理临时文件
            _tempAudioFile?.delete()
            _tempAudioFile = null
        }
    }
    
    /**
     * 获取 Access Token（带缓存）
     */
    private suspend fun getAccessToken(): String? {
        // 检查 Token 是否过期（提前 5 分钟刷新）
        if (accessToken != null && System.currentTimeMillis() < tokenExpireTime - 300000) {
            return accessToken
        }
        
        return try {
            val response = RetrofitClient.baiduOAuthApi.getAccessToken(
                clientId = BuildConfig.BAIDU_SPEECH_API_KEY,
                clientSecret = BuildConfig.BAIDU_SPEECH_SECRET_KEY
            )
            
            accessToken = response.access_token
            tokenExpireTime = System.currentTimeMillis() + (response.expires_in * 1000L)
            
            Log.d(TAG, "Token 获取成功，有效期: ${response.expires_in}秒")
            accessToken
            
        } catch (e: Exception) {
            e.printStackTrace()
            Log.e(TAG, "Token 获取失败: ${e.message}")
            null
        }
    }
    
    /**
     * 取消录音
     */
    fun cancelRecording() {
        try {
            mediaRecorder?.apply {
                stop()
                release()
            }
            mediaRecorder = null
            _tempAudioFile?.delete()
            _tempAudioFile = null
            isRecording = false
            onRecordingStateChanged(false)
            Log.d(TAG, "取消录音")
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
    
    /**
     * 释放资源
     */
    fun destroy() {
        cancelRecording()
        scope.cancel()
        Log.d(TAG, "资源已释放")
    }
}
