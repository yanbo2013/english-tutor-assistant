package com.example.myapplication.utils

import android.Manifest
import android.app.Activity
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat

/**
 * 权限帮助类
 * 兼容 Android 13+ 的运行时权限变化
 * 
 * ## Android 版本差异说明
 * 
 * ### Android 12 及以下 (API < 33)
 * - 使用 `READ_EXTERNAL_STORAGE` 和 `WRITE_EXTERNAL_STORAGE`
 * - 一个权限可以访问所有类型的文件
 * 
 * ### Android 13+ (API >= 33)
 * - 引入了细粒度的媒体权限：
 *   - `READ_MEDIA_IMAGES` - 图片
 *   - `READ_MEDIA_VIDEO` - 视频
 *   - `READ_MEDIA_AUDIO` - 音频
 *   - `READ_MEDIA_DOCUMENTS` - 文档（PDF、Word等）
 * - 不再需要 `READ/WRITE_EXTERNAL_STORAGE`
 * 
 * ## 使用示例
 * ```kotlin
 * // 在 Composable 中
 * val activity = LocalContext.current as? Activity
 * LaunchedEffect(Unit) {
 *     activity?.let {
 *         PermissionHelper.checkAndRequestCameraPermission(it)
 *     }
 * }
 * ```
 */
object PermissionHelper {
    
    // 相机权限
    private const val CAMERA = Manifest.permission.CAMERA
    
    // 录音权限
    private const val RECORD_AUDIO = Manifest.permission.RECORD_AUDIO
    
    // 存储权限 (Android 12 及以下)
    private const val WRITE_EXTERNAL_STORAGE = Manifest.permission.WRITE_EXTERNAL_STORAGE
    private const val READ_EXTERNAL_STORAGE = Manifest.permission.READ_EXTERNAL_STORAGE
    
    // 媒体权限 (Android 13+)
    private const val READ_MEDIA_IMAGES = Manifest.permission.READ_MEDIA_IMAGES
    private const val READ_MEDIA_VIDEO = Manifest.permission.READ_MEDIA_VIDEO
    private const val READ_MEDIA_AUDIO = Manifest.permission.READ_MEDIA_AUDIO
    private const val READ_MEDIA_DOCUMENTS = "android.permission.READ_MEDIA_DOCUMENTS"
    
    /**
     * 获取相机相关权限（兼容不同版本）
     */
    fun getCameraPermissions(): Array<String> {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            // Android 13+ (API 33+)
            arrayOf(CAMERA, READ_MEDIA_IMAGES)
        } else {
            // Android 12 及以下
            arrayOf(CAMERA, WRITE_EXTERNAL_STORAGE, READ_EXTERNAL_STORAGE)
        }
    }
    
    /**
     * 获取相册/图片选择权限
     */
    fun getGalleryPermissions(): Array<String> {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            arrayOf(READ_MEDIA_IMAGES)
        } else {
            arrayOf(READ_EXTERNAL_STORAGE)
        }
    }
    
    /**
     * 获取文档读取权限
     */
    fun getDocumentPermissions(): Array<String> {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            arrayOf(READ_MEDIA_DOCUMENTS)
        } else {
            arrayOf(READ_EXTERNAL_STORAGE)
        }
    }
    
    /**
     * 获取录音权限
     */
    fun getRecordAudioPermissions(): Array<String> {
        return arrayOf(RECORD_AUDIO)
    }
    
    /**
     * 检查是否拥有指定权限
     */
    fun hasPermission(activity: Activity, permission: String): Boolean {
        return ContextCompat.checkSelfPermission(
            activity,
            permission
        ) == PackageManager.PERMISSION_GRANTED
    }
    
    /**
     * 检查是否拥有一组权限
     */
    fun hasPermissions(activity: Activity, permissions: Array<String>): Boolean {
        return permissions.all { permission ->
            hasPermission(activity, permission)
        }
    }
    
    /**
     * 请求单个权限
     */
    fun requestPermission(activity: Activity, permission: String, requestCode: Int = 1001) {
        ActivityCompat.requestPermissions(
            activity,
            arrayOf(permission),
            requestCode
        )
    }
    
    /**
     * 请求一组权限
     */
    fun requestPermissions(activity: Activity, permissions: Array<String>, requestCode: Int = 1001) {
        ActivityCompat.requestPermissions(
            activity,
            permissions,
            requestCode
        )
    }
    
    /**
     * 检查并请求相机权限
     * @return true 如果已有权限，false 如果需要请求权限
     */
    fun checkAndRequestCameraPermission(activity: Activity, requestCode: Int = 1001): Boolean {
        val permissions = getCameraPermissions()
        return if (hasPermissions(activity, permissions)) {
            true
        } else {
            requestPermissions(activity, permissions, requestCode)
            false
        }
    }
    
    /**
     * 检查并请求录音权限
     */
    fun checkAndRequestRecordAudioPermission(activity: Activity, requestCode: Int = 1002): Boolean {
        val permissions = getRecordAudioPermissions()
        return if (hasPermissions(activity, permissions)) {
            true
        } else {
            requestPermissions(activity, permissions, requestCode)
            false
        }
    }
    
    /**
     * 检查并请求文档读取权限
     */
    fun checkAndRequestDocumentPermission(activity: Activity, requestCode: Int = 1003): Boolean {
        val permissions = getDocumentPermissions()
        return if (hasPermissions(activity, permissions)) {
            true
        } else {
            requestPermissions(activity, permissions, requestCode)
            false
        }
    }
    
    /**
     * 判断是否需要显示权限说明（用户之前拒绝过）
     */
    fun shouldShowRationale(activity: Activity, permission: String): Boolean {
        return ActivityCompat.shouldShowRequestPermissionRationale(activity, permission)
    }
}
