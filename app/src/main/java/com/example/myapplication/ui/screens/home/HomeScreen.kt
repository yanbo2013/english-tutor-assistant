package com.example.myapplication.ui.screens.home

import android.app.Activity
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.myapplication.utils.PermissionHelper

/**
 * 首页
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    onNavigateToCamera: () -> Unit,
    onNavigateToDocument: () -> Unit,
    onNavigateToHistory: () -> Unit,
    onNavigateToSettings: () -> Unit
) {
    val context = LocalContext.current
    val activity = context as? Activity
    
    // 权限检查状态
    var showPermissionDialog by remember { mutableStateOf(false) }
    var showSourceSelectionDialog by remember { mutableStateOf(false) }
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { 
                    Text(
                        text = "AI英语阅读伴侣",
                        fontWeight = FontWeight.Bold
                    ) 
                },
                actions = {
                    IconButton(onClick = onNavigateToSettings) {
                        Icon(
                            imageVector = Icons.Default.Settings,
                            contentDescription = "设置"
                        )
                    }
                }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = "欢迎使用",
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold
            )
            
            Spacer(modifier = Modifier.height(32.dp))
            
            Button(
                onClick = {
                    // 检查相机和存储权限
                    activity?.let {
                        val hasCameraPermission = PermissionHelper.hasPermission(it, android.Manifest.permission.CAMERA)
                        val hasStoragePermission = if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.TIRAMISU) {
                            PermissionHelper.hasPermission(it, android.Manifest.permission.READ_MEDIA_IMAGES)
                        } else {
                            PermissionHelper.hasPermission(it, android.Manifest.permission.READ_EXTERNAL_STORAGE)
                        }
                        
                        if (hasCameraPermission && hasStoragePermission) {
                            // 已有权限，显示选择对话框
                            showSourceSelectionDialog = true
                        } else {
                            // 请求权限
                            PermissionHelper.checkAndRequestCameraPermission(it)
                        }
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp)
            ) {
                Text(text = "📷 拍照上传练习", fontSize = 16.sp)
            }
            
            Spacer(modifier = Modifier.height(12.dp))
            
            Button(
                onClick = onNavigateToDocument,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.secondaryContainer,
                    contentColor = MaterialTheme.colorScheme.onSecondaryContainer
                )
            ) {
                Text(text = "📄 上传文档练习", fontSize = 16.sp)
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            OutlinedButton(
                onClick = onNavigateToHistory,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp)
            ) {
                Text(text = "查看历史记录", fontSize = 16.sp)
            }
        }
    }
    
    // 图片来源选择对话框
    if (showSourceSelectionDialog) {
        AlertDialog(
            onDismissRequest = { showSourceSelectionDialog = false },
            title = { Text("选择图片来源") },
            text = { Text("请选择拍照或从相册选择") },
            confirmButton = {
                Button(onClick = {
                    showSourceSelectionDialog = false
                    onNavigateToCamera() // TODO: 传递图片源类型
                }) {
                    Text("📷 拍照")
                }
            },
            dismissButton = {
                OutlinedButton(onClick = {
                    showSourceSelectionDialog = false
                    // TODO: 打开相册选择器
                }) {
                    Text("🖼️ 相册")
                }
            }
        )
    }
}
