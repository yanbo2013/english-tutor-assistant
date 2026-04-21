package com.example.myapplication.utils

import android.content.Context
import android.media.MediaRecorder
import android.os.Build
import java.io.File

/**
 * 音频录制帮助类
 */
class AudioHelper(private val context: Context) {
    
    private var mediaRecorder: MediaRecorder? = null
    private var outputFile: File? = null
    
    /**
     * 开始录音
     */
    fun startRecording(fileName: String): String? {
        return try {
            val audioDir = File(context.filesDir, "audio_records")
            if (!audioDir.exists()) {
                audioDir.mkdirs()
            }
            
            outputFile = File(audioDir, "$fileName.m4a")
            
            mediaRecorder = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                MediaRecorder(context)
            } else {
                @Suppress("DEPRECATION")
                MediaRecorder()
            }.apply {
                setAudioSource(MediaRecorder.AudioSource.MIC)
                setOutputFormat(MediaRecorder.OutputFormat.MPEG_4)
                setAudioEncoder(MediaRecorder.AudioEncoder.AAC)
                setAudioEncodingBitRate(128000)
                setAudioSamplingRate(44100)
                setOutputFile(outputFile?.absolutePath)
                
                prepare()
                start()
            }
            
            outputFile?.absolutePath
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }
    
    /**
     * 停止录音
     */
    fun stopRecording() {
        try {
            mediaRecorder?.apply {
                stop()
                release()
            }
            mediaRecorder = null
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
    
    /**
     * 取消录音并删除文件
     */
    fun cancelRecording() {
        stopRecording()
        outputFile?.delete()
        outputFile = null
    }
    
    /**
     * 释放资源
     */
    fun release() {
        mediaRecorder?.release()
        mediaRecorder = null
    }
}
