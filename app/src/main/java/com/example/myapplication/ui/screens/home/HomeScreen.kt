package com.example.myapplication.ui.screens.home

import android.Manifest
import android.app.Activity
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.myapplication.domain.model.Session
import com.example.myapplication.domain.model.TextSegment
import com.example.myapplication.ui.components.AttachFileDialog
import com.example.myapplication.ui.components.ChatInputBar
import com.example.myapplication.ui.components.ChatListItem
import com.example.myapplication.ui.components.EmptyChatList
import com.example.myapplication.utils.PermissionHelper

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    onNavigateToCamera: () -> Unit,
    onNavigateToDocument: () -> Unit,
    onNavigateToSettings: () -> Unit,
    onSessionSelected: (Long) -> Unit
) {
    val context = LocalContext.current
    val activity = context as? Activity
    
    var showAttachDialog by remember { mutableStateOf(false) }
    var showNewSessionDialog by remember { mutableStateOf(false) }
    var inputText by remember { mutableStateOf("") }
    
    val sessions = remember {
        mutableStateListOf(
            Session(
                id = 1,
                timestamp = System.currentTimeMillis() - 3600000,
                imageUrl = null,
                recognizedText = "这是一段测试文本，用于展示聊天列表的预览效果。这段文字会被截断显示。",
                segments = listOf(
                    TextSegment(index = 1, text = "段落1")
                ),
                sessionType = "practice"
            ),
            Session(
                id = 2,
                timestamp = System.currentTimeMillis() - 7200000,
                imageUrl = null,
                recognizedText = "复习昨天学习的内容，重点练习发音和语调。",
                segments = listOf(),
                sessionType = "review"
            )
        )
    }
    
    val hasSessions = sessions.isNotEmpty()
    
    LaunchedEffect(Unit) {
        if (!hasSessions) {
            showNewSessionDialog = true
        }
    }
    
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
                    if (hasSessions) {
                        IconButton(onClick = { showNewSessionDialog = true }) {
                            Icon(
                                imageVector = Icons.Default.Add,
                                contentDescription = "新建会话"
                            )
                        }
                    }
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
        ) {
            if (!hasSessions) {
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth(),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Text(
                            text = "开始您的第一次练习",
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "点击下方按钮创建新会话",
                            fontSize = 14.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            } else {
                LazyColumn(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth(),
                    reverseLayout = false
                ) {
                    items(
                        items = sessions,
                        key = { session -> session.id }
                    ) { session ->
                        ChatListItem(
                            session = session,
                            onClick = {
                                onSessionSelected(session.id)
                            }
                        )
                        HorizontalDivider(
                            modifier = Modifier.padding(horizontal = 16.dp),
                            color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f)
                        )
                    }
                }
            }
            
            ChatInputBar(
                onAttachClick = {
                    showAttachDialog = true
                },
                onVoiceClick = {
                    
                },
                onSendClick = {
                    
                },
                inputText = inputText,
                onInputTextChange = { inputText = it }
            )
        }
    }
    
    if (showNewSessionDialog) {
        AttachFileDialog(
            onDismiss = { showNewSessionDialog = false },
            onCameraClick = {
                activity?.let {
                    val hasCameraPermission = PermissionHelper.hasPermission(it, Manifest.permission.CAMERA)
                    val hasStoragePermission = if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.TIRAMISU) {
                        PermissionHelper.hasPermission(it, Manifest.permission.READ_MEDIA_IMAGES)
                    } else {
                        PermissionHelper.hasPermission(it, Manifest.permission.READ_EXTERNAL_STORAGE)
                    }
                    
                    if (hasCameraPermission && hasStoragePermission) {
                        onNavigateToCamera()
                    } else {
                        PermissionHelper.checkAndRequestCameraPermission(it)
                    }
                }
            },
            onGalleryClick = {
                
            },
            onDocumentClick = {
                onNavigateToDocument()
            }
        )
    }
    
    if (showAttachDialog) {
        AttachFileDialog(
            onDismiss = { showAttachDialog = false },
            onCameraClick = {
                activity?.let {
                    val hasCameraPermission = PermissionHelper.hasPermission(it, Manifest.permission.CAMERA)
                    val hasStoragePermission = if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.TIRAMISU) {
                        PermissionHelper.hasPermission(it, Manifest.permission.READ_MEDIA_IMAGES)
                    } else {
                        PermissionHelper.hasPermission(it, Manifest.permission.READ_EXTERNAL_STORAGE)
                    }
                    
                    if (hasCameraPermission && hasStoragePermission) {
                        onNavigateToCamera()
                    } else {
                        PermissionHelper.checkAndRequestCameraPermission(it)
                    }
                }
            },
            onGalleryClick = {
                
            },
            onDocumentClick = {
                onNavigateToDocument()
            }
        )
    }
}
