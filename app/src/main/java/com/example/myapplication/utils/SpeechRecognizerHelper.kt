package com.example.myapplication.utils

import android.content.Context
import android.media.MediaRecorder
import android.os.Build
import android.util.Base64
import android.util.Log
import com.example.myapplication.BuildConfig
import com.example.myapplication.network.RetrofitClient
import com.example.myapplication.network.SpeechRecognitionRequest
import kotlinx.coroutines.*
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
        // AMR_NB 格式的固定采样率为 8000Hz
        private const val SAMPLE_RATE = 8000 // 采样率（AMR_NB 必须是 8000）
        
        // 百度语音识别 API 配置
        private const val DEV_PID = 80001 // 普通话+英语混合识别
        private const val AUDIO_FORMAT = "amr" // 音频格式
        private const val CHANNEL = 1 // 单声道
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
            Log.d(TAG, "========== 开始语音识别 ==========")
            
            // 1. 获取 Access Token
            Log.d(TAG, "步骤1: 获取 Access Token")
            val token = getAccessToken()
            if (token == null) {
                Log.e(TAG, "Token 获取失败")
                withContext(Dispatchers.Main) {
                    onError("获取 Token 失败")
                }
                return
            }
            Log.d(TAG, "Token 获取成功: ${token.take(10)}...")
            
            // 2. 读取音频数据（从临时文件）
            Log.d(TAG, "步骤2: 读取音频数据")
            val audioData = _tempAudioFile?.readBytes()
            if (audioData == null || audioData.isEmpty()) {
                Log.e(TAG, "音频数据为空")
                withContext(Dispatchers.Main) {
                    onError("音频数据为空")
                }
                return
            }
            
            Log.d(TAG, "音频数据大小: ${audioData.size} bytes")
            Log.d(TAG, "临时文件路径: ${_tempAudioFile?.absolutePath}")
            
            // 3. Base64 编码音频数据
            Log.d(TAG, "步骤3: Base64 编码音频数据")
            val base64Speech = Base64.encodeToString(audioData, Base64.NO_WRAP)
            Log.d(TAG, "Base64 编码后长度: ${base64Speech.length}")
            
            // 4. 构建请求体（JSON 格式）
            Log.d(TAG, "步骤4: 构建 JSON 请求体")
            val request = SpeechRecognitionRequest(
                format = AUDIO_FORMAT,
                rate = SAMPLE_RATE,
                channel = CHANNEL,
                cuid = deviceId,
                dev_pid = DEV_PID,
                speech = base64Speech,
                len = audioData.size
            )
            Log.d(TAG, "请求参数: format=${request.format}, rate=${request.rate}, channel=${request.channel}")
            Log.d(TAG, "设备 ID (CUID): $deviceId")
            
            // 5. 调用识别 API
            Log.d(TAG, "步骤5: 调用百度 API")
            val response = RetrofitClient.baiduSpeechApi.recognizeSpeech(token, deviceId, request)
            Log.d(TAG, "API 响应接收成功")
            
            // 6. 处理结果
            withContext(Dispatchers.Main) {
                Log.d(TAG, "步骤6: 处理识别结果")
                Log.d(TAG, "err_no: ${response.err_no}")
                Log.d(TAG, "err_msg: ${response.err_msg}")
                Log.d(TAG, "result: ${response.result}")
                
                if (response.err_no == 0 && response.result != null && response.result.isNotEmpty()) {
                    val recognizedText = response.result.joinToString("")
                    Log.d(TAG, "✅ 识别成功: $recognizedText")
                    onResult(recognizedText)
                } else {
                    val errorMsg = response.err_msg ?: "未知错误"
                    Log.e(TAG, "❌ 识别失败: err_no=${response.err_no}, msg=$errorMsg")
                    onError("识别失败: $errorMsg (错误码: ${response.err_no})")
                }
            }
            
            Log.d(TAG, "========== 语音识别结束 ==========\n")
            
        } catch (e: Exception) {
            e.printStackTrace()
            Log.e(TAG, "❌ 网络请求异常: ${e.javaClass.simpleName}")
            Log.e(TAG, "异常消息: ${e.message}")
            Log.e(TAG, "异常堆栈:", e)
            withContext(Dispatchers.Main) {
                onError("网络请求失败: ${e.message}")
            }
        } finally {
            // 清理临时文件
            Log.d(TAG, "清理临时文件")
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
            Log.d(TAG, "使用缓存的 Token")
            return accessToken
        }
        
        Log.d(TAG, "开始获取新的 Access Token")
        Log.d(TAG, "API Key: ${BuildConfig.BAIDU_SPEECH_API_KEY.take(10)}...")
        
        return try {
            val response = RetrofitClient.baiduOAuthApi.getAccessToken(
                clientId = BuildConfig.BAIDU_SPEECH_API_KEY,
                clientSecret = BuildConfig.BAIDU_SPEECH_SECRET_KEY
            )
            
            accessToken = response.access_token
            tokenExpireTime = System.currentTimeMillis() + (response.expires_in * 1000L)
            
            Log.d(TAG, "✅ Token 获取成功，有效期: ${response.expires_in}秒")
            Log.d(TAG, "Token 过期时间: ${java.text.SimpleDateFormat("yyyy-MM-dd HH:mm:ss", java.util.Locale.getDefault()).format(java.util.Date(tokenExpireTime))}")
            accessToken
            
        } catch (e: Exception) {
            e.printStackTrace()
            Log.e(TAG, "❌ Token 获取失败: ${e.javaClass.simpleName}")
            Log.e(TAG, "错误消息: ${e.message}")
            Log.e(TAG, "错误堆栈:", e)
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
