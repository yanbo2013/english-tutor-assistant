package com.example.myapplication.ui.components

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.*

/**
 * 权限请求助手
 * 用于在 Compose 中优雅地处理权限请求
 */
@Composable
fun rememberPermissionLauncher(
    onPermissionGranted: () -> Unit = {},
    onPermissionDenied: () -> Unit = {}
): Pair<(String) -> Unit, (String) -> Boolean> {
    
    var pendingPermission by remember { mutableStateOf<String?>(null) }
    
    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted: Boolean ->
        if (isGranted) {
            onPermissionGranted()
        } else {
            onPermissionDenied()
        }
        pendingPermission = null
    }
    
    val requestPermission: (String) -> Unit = { permission ->
        pendingPermission = permission
        launcher.launch(permission)
    }
    
    val checkPermission: (String) -> Boolean = { permission ->
        // 这个函数需要在 Activity 中实现，这里返回 false 作为占位
        false
    }
    
    return Pair(requestPermission, checkPermission)
}

/**
 * 多权限请求助手
 */
@Composable
fun rememberMultiplePermissionsLauncher(
    onAllPermissionsGranted: () -> Unit = {},
    onSomePermissionsDenied: (List<String>) -> Unit = {}
): (Array<String>) -> Unit {
    
    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions: Map<String, Boolean> ->
        val allGranted = permissions.values.all { it }
        if (allGranted) {
            onAllPermissionsGranted()
        } else {
            val deniedPermissions = permissions.filter { !it.value }.keys.toList()
            onSomePermissionsDenied(deniedPermissions)
        }
    }
    
    return { permissions ->
        launcher.launch(permissions)
    }
}
