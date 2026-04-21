package com.example.myapplication.ui.screens.chat

import android.Manifest
import android.app.Activity
import android.graphics.Bitmap
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.Send
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.example.myapplication.utils.PermissionHelper
import com.example.myapplication.utils.SpeechRecognizerHelper
import kotlinx.coroutines.launch

/**
 * 消息数据类
 */
data class ChatMessage(
    val id: String = java.util.UUID.randomUUID().toString(),
    val content: String,
    val isUser: Boolean, // true 为用户消息，false 为 AI 消息
    val timestamp: Long = System.currentTimeMillis(),
    val imageUri: Uri? = null // 用户消息可能附带图片
)

/**
 * AI对话练习页（Chat风格 - 类似豆包）
 * 
 * 布局结构：
 * - 顶部：标题栏
 * - 中间：聊天记录列表（可滚动）
 * - 底部：输入区域
 *   - 图片缩略图（如果有的话）
 *   - 文本输入框 + 录音按钮 + 发送按钮
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ImageChatScreen(
    imageUri: Uri?,
    imageBitmap: Bitmap? = null,
    onNavigateBack: () -> Unit,
    onSubmitPrompt: (prompt: String, imageUri: Uri?) -> Unit
) {
    val context = LocalContext.current
    val activity = context as? Activity
    
    var promptText by remember { mutableStateOf("") }
    var currentImageUri by remember { mutableStateOf(imageUri) }
    var currentImageBitmap by remember { mutableStateOf(imageBitmap) }
    
    // 聊天记录列表
    var messages by remember { mutableStateOf(listOf<ChatMessage>()) }
    
    // 是否正在录音
    var isRecording by remember { mutableStateOf(false) }
    var hasRecordPermission by remember { mutableStateOf(false) }
    var showPermissionDialog by remember { mutableStateOf(false) }
    var recognitionError by remember { mutableStateOf<String?>(null) }
    
    // Snackbar 状态
    val snackbarHostState = remember { SnackbarHostState() }
    val coroutineScope = rememberCoroutineScope()
    
    // 创建语音识别助手
    val speechRecognizerHelper = remember {
        SpeechRecognizerHelper(
            context = context,
            onResult = { recognizedText ->
                // 识别成功，将文字填入输入框
                promptText = if (promptText.isNotBlank()) {
                    "$promptText $recognizedText"
                } else {
                    recognizedText
                }
                isRecording = false
            },
            onError = { error ->
                recognitionError = error
                isRecording = false
                // 显示错误提示
                coroutineScope.launch {
                    snackbarHostState.showSnackbar(error)
                }
            },
            onRecordingStateChanged = { recording ->
                isRecording = recording
            }
        )
    }
    
    // 页面销毁时清理资源
    DisposableEffect(Unit) {
        onDispose {
            speechRecognizerHelper.destroy()
        }
    }
    
    // 权限请求启动器
    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        hasRecordPermission = permissions.values.all { it }
        if (hasRecordPermission) {
            // 授权成功，开始录音
            isRecording = true
        } else {
            // 授权失败，显示提示
            showPermissionDialog = true
        }
    }
    
    // 页面加载时检查录音权限
    LaunchedEffect(Unit) {
        activity?.let {
            hasRecordPermission = PermissionHelper.hasPermission(
                it,
                Manifest.permission.RECORD_AUDIO
            )
        }
    }
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("AI阅读练习", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(
                            imageVector = Icons.Default.ArrowBack,
                            contentDescription = "返回"
                        )
                    }
                }
            )
        },
        snackbarHost = {
            SnackbarHost(hostState = snackbarHostState)
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            // ===== 聊天记录区域 =====
            LazyColumn(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
                contentPadding = PaddingValues(vertical = 16.dp)
            ) {
                items(messages) { message ->
                    ChatMessageItem(message = message)
                }
                
                // 如果没有消息，显示欢迎提示
                if (messages.isEmpty()) {
                    item {
                        Box(
                            modifier = Modifier.fillMaxHeight(),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Text(
                                    text = "👋 你好！",
                                    style = MaterialTheme.typography.headlineSmall,
                                    fontWeight = FontWeight.Bold
                                )
                                Spacer(modifier = Modifier.height(8.dp))
                                Text(
                                    text = "上传了图片，可以问我问题哦~",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                }
            }
            
            // ===== 底部输入区域 =====
            Surface(
                modifier = Modifier.fillMaxWidth(),
                tonalElevation = 2.dp,
                color = MaterialTheme.colorScheme.surface
            ) {
                Column(
                    modifier = Modifier.padding(12.dp)
                ) {
                    // 显示图片缩略图（如果有）
                    if (currentImageUri != null || currentImageBitmap != null) {
                        Card(
                            modifier = Modifier
                                .wrapContentWidth()
                                .height(80.dp),
                            shape = RoundedCornerShape(12.dp),
                            colors = CardDefaults.cardColors(
                                containerColor = MaterialTheme.colorScheme.surfaceVariant
                            )
                        ) {
                            Box {
                                // 显示图片
                                val bitmapToShow = currentImageBitmap
                                if (bitmapToShow != null) {
                                    Image(
                                        bitmap = bitmapToShow.asImageBitmap(),
                                        contentDescription = "上传的图片",
                                        modifier = Modifier
                                            .fillMaxHeight()
                                            .width(80.dp)
                                            .clip(RoundedCornerShape(12.dp)),
                                        contentScale = ContentScale.Crop
                                    )
                                } else if (currentImageUri != null) {
                                    AsyncImage(
                                        model = currentImageUri,
                                        contentDescription = "上传的图片",
                                        modifier = Modifier
                                            .fillMaxHeight()
                                            .width(80.dp)
                                            .clip(RoundedCornerShape(12.dp)),
                                        contentScale = ContentScale.Crop
                                    )
                                }
                                
                                // 删除按钮
                                IconButton(
                                    onClick = {
                                        currentImageUri = null
                                        currentImageBitmap = null
                                    },
                                    modifier = Modifier
                                        .align(Alignment.TopEnd)
                                        .size(24.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Delete,
                                        contentDescription = "删除图片",
                                        tint = MaterialTheme.colorScheme.error,
                                        modifier = Modifier.size(16.dp)
                                    )
                                }
                            }
                        }
                        
                        Spacer(modifier = Modifier.height(8.dp))
                    }
                    
                    // 输入框行
                    Column(
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        // 录音状态提示
                        if (isRecording) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(bottom = 8.dp),
                                horizontalArrangement = Arrangement.Center,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                // 录音指示器动画
                                repeat(3) { index ->
                                    Box(
                                        modifier = Modifier
                                            .size(8.dp)
                                            .background(
                                                MaterialTheme.colorScheme.error,
                                                CircleShape
                                            )
                                    )
                                    if (index < 2) {
                                        Spacer(modifier = Modifier.width(4.dp))
                                    }
                                }
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "正在聆听...",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.error
                                )
                            }
                        }
                        
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                        // 文本输入框
                        TextField(
                            value = promptText,
                            onValueChange = { promptText = it },
                            placeholder = { 
                                if (isRecording) {
                                    Text("🎤 正在聆听...")
                                } else {
                                    Text("输入问题，例如：朗读前两段")
                                }
                            },
                            modifier = Modifier.weight(1f),
                            colors = TextFieldDefaults.colors(
                                focusedContainerColor = MaterialTheme.colorScheme.surface,
                                unfocusedContainerColor = MaterialTheme.colorScheme.surface,
                                focusedIndicatorColor = if (isRecording) MaterialTheme.colorScheme.error
                                                         else MaterialTheme.colorScheme.primary,
                                unfocusedIndicatorColor = MaterialTheme.colorScheme.outline
                            ),
                            maxLines = 4,
                            shape = RoundedCornerShape(24.dp),
                            enabled = !isRecording // 录音时禁用输入
                        )
                        
                        // 录音按钮
                        IconButton(
                            onClick = {
                                if (!hasRecordPermission) {
                                    // 没有权限，请求授权
                                    activity?.let {
                                        PermissionHelper.checkAndRequestRecordAudioPermission(it)
                                    }
                                } else {
                                    // 已有权限，切换录音状态
                                    if (isRecording) {
                                        // 停止录音并识别
                                        speechRecognizerHelper.stopRecordingAndRecognize()
                                    } else {
                                        // 开始录音
                                        recognitionError = null
                                        speechRecognizerHelper.startRecording()
                                    }
                                }
                            },
                            modifier = Modifier
                                .size(48.dp)
                                .background(
                                    if (isRecording) MaterialTheme.colorScheme.errorContainer
                                    else MaterialTheme.colorScheme.primaryContainer,
                                    CircleShape
                                )
                        ) {
                            Icon(
                                imageVector = Icons.Default.Mic,
                                contentDescription = if (isRecording) "停止录音" else "语音输入",
                                tint = if (isRecording) MaterialTheme.colorScheme.onErrorContainer
                                else MaterialTheme.colorScheme.onPrimaryContainer
                            )
                        }
                        
                        // 发送按钮
                        IconButton(
                            onClick = {
                                if (promptText.isNotBlank() || currentImageUri != null) {
                                    val newMessage = ChatMessage(
                                        content = promptText,
                                        isUser = true,
                                        imageUri = currentImageUri
                                    )
                                    messages = messages + newMessage
                                    
                                    // 调用提交回调
                                    onSubmitPrompt(promptText, currentImageUri)
                                    
                                    // 清空输入
                                    promptText = ""
                                    // 可选：发送后清除图片
                                    // currentImageUri = null
                                    // currentImageBitmap = null
                                }
                            },
                            enabled = promptText.isNotBlank() || currentImageUri != null
                        ) {
                            Icon(
                                imageVector = Icons.Default.Send,
                                contentDescription = "发送",
                                tint = if (promptText.isNotBlank() || currentImageUri != null) {
                                    MaterialTheme.colorScheme.primary
                                } else {
                                    MaterialTheme.colorScheme.onSurfaceVariant
                                }
                            )
                        }
                        } // 关闭 Row
                    } // 关闭 Column
                }
            }
        }
    }
    
    // 权限授权失败提示对话框
    if (showPermissionDialog) {
        AlertDialog(
            onDismissRequest = { showPermissionDialog = false },
            title = { Text("需要录音权限") },
            text = { 
                Text("为了使用语音输入功能，需要授予录音权限。请在系统设置中手动开启。") 
            },
            confirmButton = {
                Button(onClick = {
                    showPermissionDialog = false
                }) {
                    Text("知道了")
                }
            },
            dismissButton = {
                OutlinedButton(onClick = {
                    showPermissionDialog = false
                }) {
                    Text("取消")
                }
            }
        )
    }
}

/**
 * 聊天消息项
 */
@Composable
fun ChatMessageItem(
    message: ChatMessage,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = if (message.isUser) Arrangement.End else Arrangement.Start
    ) {
        Card(
            colors = CardDefaults.cardColors(
                containerColor = if (message.isUser) {
                    MaterialTheme.colorScheme.primaryContainer
                } else {
                    MaterialTheme.colorScheme.surfaceVariant
                }
            ),
            shape = RoundedCornerShape(
                topStart = 16.dp,
                topEnd = 16.dp,
                bottomStart = if (message.isUser) 16.dp else 4.dp,
                bottomEnd = if (message.isUser) 4.dp else 16.dp
            )
        ) {
            Column(
                modifier = Modifier.padding(12.dp)
            ) {
                // 如果消息包含图片，先显示图片
                message.imageUri?.let { uri ->
                    AsyncImage(
                        model = uri,
                        contentDescription = "消息中的图片",
                        modifier = Modifier
                            .width(200.dp)
                            .height(150.dp)
                            .clip(RoundedCornerShape(8.dp)),
                        contentScale = ContentScale.Crop
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                }
                
                // 显示文本内容
                if (message.content.isNotBlank()) {
                    Text(
                        text = message.content,
                        style = MaterialTheme.typography.bodyLarge,
                        color = if (message.isUser) {
                            MaterialTheme.colorScheme.onPrimaryContainer
                        } else {
                            MaterialTheme.colorScheme.onSurfaceVariant
                        }
                    )
                }
            }
        }
    }
}
