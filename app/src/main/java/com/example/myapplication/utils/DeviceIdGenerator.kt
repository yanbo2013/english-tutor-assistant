package com.example.myapplication.utils

import android.content.Context
import android.provider.Settings
import android.util.Log
import java.util.UUID

/**
 * 设备唯一标识生成工具类
 * 
 * 功能：
 * 1. 提供多种设备标识生成方案
 * 2. 自动验证标识有效性
 * 3. 支持降级策略，确保始终返回有效标识
 * 4. 内存缓存，避免重复计算
 * 
 * 使用场景：
 * - 百度语音识别 CUID
 * - 用户行为追踪
 * - 设备指纹识别
 * - API 请求标识
 * 
 * 生成方案：
 * 1. ANDROID_ID - 系统级唯一标识（推荐）
 * 2. UUID v4 - 随机生成（降级方案）
 */
object DeviceIdGenerator {
    
    private const val TAG = "DeviceIdGenerator"
    
    // 已知的 Android ID bug 值
    private const val ANDROID_ID_BUG_VALUE = "9774d56d682e549c"
    
    // 内存缓存（应用生命周期内有效）
    @Volatile
    private var cachedDeviceId: String? = null
    
    /**
     * 获取设备唯一标识（推荐使用）
     * 
     * 优先级策略：
     * 1. 内存缓存（如果存在）
     * 2. ANDROID_ID（系统级唯一标识）
     * 3. 随机 UUID（降级方案）
     * 
     * @param context 应用上下文
     * @param prefix 可选前缀，用于区分不同业务场景
     * @return 设备唯一标识字符串
     */
    fun getDeviceId(context: Context, prefix: String? = null): String {
        // 检查缓存
        cachedDeviceId?.let { cached ->
            Log.d(TAG, "使用缓存的设备 ID")
            return if (prefix != null) "$prefix-$cached" else cached
        }
        
        // 生成新的设备 ID
        val deviceId = generateWithFallback(context)
        
        // 存入缓存
        cachedDeviceId = deviceId
        
        Log.d(TAG, "生成新设备 ID: ${formatForLog(deviceId)}")
        
        return if (prefix != null) "$prefix-$deviceId" else deviceId
    }
    
    /**
     * 带降级策略的设备 ID 生成
     * 
     * @param context 应用上下文
     * @return 有效的设备标识
     */
    private fun generateWithFallback(context: Context): String {
        val startTime = System.currentTimeMillis()
        
        // 方案1：尝试 ANDROID_ID
        val androidId = getAndroidId(context)
        if (isValidId(androidId)) {
            val duration = System.currentTimeMillis() - startTime
            Log.d(TAG, "使用 ANDROID_ID: ${formatForLog(androidId!!)} (耗时: ${duration}ms)")
            return androidId!!
        }
        
        // 方案2：生成随机 UUID
        val uuid = generateUUID()
        val duration = System.currentTimeMillis() - startTime
        Log.w(TAG, "ANDROID_ID 无效，使用 UUID: $uuid (耗时: ${duration}ms)")
        return uuid
    }
    
    /**
     * 获取 Android ID
     * 
     * @param context 应用上下文
     * @return Android ID，可能为 null 或无效值
     */
    private fun getAndroidId(context: Context): String? {
        return try {
            Settings.Secure.getString(
                context.contentResolver,
                Settings.Secure.ANDROID_ID
            )
        } catch (e: Exception) {
            Log.e(TAG, "获取 ANDROID_ID 失败: ${e.message}")
            null
        }
    }
    
    /**
     * 生成 UUID v4
     * 
     * @return 随机 UUID 字符串
     */
    private fun generateUUID(): String {
        return UUID.randomUUID().toString()
    }
    
    /**
     * 验证设备标识是否有效
     * 
     * 检查规则：
     * 1. 不为 null
     * 2. 不为空字符串
     * 3. 不是已知的 bug 值
     * 4. 长度合理（至少 8 位）
     * 
     * @param id 待验证的设备标识
     * @return true 如果标识有效
     */
    fun isValidId(id: String?): Boolean {
        return id != null &&
                id.isNotEmpty() &&
                id != ANDROID_ID_BUG_VALUE &&
                id.length >= 8
    }
    
    /**
     * 清除缓存（用于测试或强制刷新）
     */
    fun clearCache() {
        cachedDeviceId = null
        Log.d(TAG, "设备 ID 缓存已清除")
    }
    
    /**
     * 格式化设备标识（用于日志显示）
     * 
     * @param id 设备标识
     * @return 格式化后的字符串（隐藏部分字符）
     */
    fun formatForLog(id: String): String {
        return if (id.length > 8) {
            "${id.take(4)}****${id.takeLast(4)}"
        } else {
            "****"
        }
    }
}
