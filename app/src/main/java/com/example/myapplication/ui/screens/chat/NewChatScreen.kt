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
import androidx.compose.material.icons.filled.*
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
import com.example.myapplication.ui.components.ChatMessage
import com.example.myapplication.ui.components.ChatMessageItem
import com.example.myapplication.utils.PermissionHelper
import com.example.myapplication.utils.SpeechRecognizerHelper
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

/**
 * 练习页面
 * 
 * 这个页面用于新的聊天会话，采用微信聊天风格：
 * - 发送消息后不跳转页面
 * - 消息显示在聊天列表中（用户消息在右，AI消息在左）
 * - 等待LLM返回信息并展示
 * 
 * 页面结构：
 * - 顶部导航栏：返回按钮和标题"练习页面"
 * - 聊天列表区域：显示历史消息（用户消息和AI回复）
 * - 底部输入栏：文本输入框、附件按钮、录音按钮、发送按钮
 * 
 * 消息样式：
 * - 用户消息：右侧对齐，蓝色气泡，显示"[我]"标识和头像
 * - AI消息：左侧对齐，灰色气泡，显示"[AI回复]"标识和头像
 * 
 * 功能特点：
 * 1. 点击附件按钮时才显示附件上传弹窗（拍照/相册/文档）
 * 2. 支持语音输入
 * 3. 支持发送文本消息
 * 4. 发送消息后留在当前页面，像微信聊天一样
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NewChatScreen(
    onNavigateBack: () -> Unit,
    onNavigateToCamera: () -> Unit,
    onNavigateToDocument: () -> Unit,
    onSubmitPrompt: (prompt: String, imageUri: Uri?) -> Unit
) {
    val context = LocalContext.current
    val activity = context as? Activity
    
    // ===== 状态变量 =====
    var promptText by remember { mutableStateOf("") }
    var currentImageUri by remember { mutableStateOf<Uri?>(null) }
    var currentImageBitmap by remember { mutableStateOf<Bitmap?>(null) }
    var messages by remember { mutableStateOf(listOf<ChatMessage>()) }
    var isRecording by remember { mutableStateOf(false) }
    var hasRecordPermission by remember { mutableStateOf(false) }
    var showPermissionDialog by remember { mutableStateOf(false) }
    var recognitionError by remember { mutableStateOf<String?>(null) }
    var showAttachDialog by remember { mutableStateOf(false) }
    
    // ===== 协程和Snackbar =====
    val snackbarHostState = remember { SnackbarHostState() }
    val coroutineScope = rememberCoroutineScope()
    
    // ===== 语音识别助手 =====
    val speechRecognizerHelper = remember {
        SpeechRecognizerHelper(
            context = context,
            onResult = { recognizedText ->
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
                coroutineScope.launch {
                    snackbarHostState.showSnackbar(error)
                }
            },
            onRecordingStateChanged = { recording ->
                isRecording = recording
            }
        )
    }
    
    // ===== 生命周期管理 =====
    DisposableEffect(Unit) {
        onDispose {
            speechRecognizerHelper.destroy()
        }
    }
    
    // ===== 权限请求启动器 =====
    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        hasRecordPermission = permissions.values.all { it }
        if (hasRecordPermission) {
            isRecording = true
        } else {
            showPermissionDialog = true
        }
    }
    
    // ===== 页面初始化 =====
    LaunchedEffect(Unit) {
        activity?.let {
            hasRecordPermission = PermissionHelper.hasPermission(
                it,
                Manifest.permission.RECORD_AUDIO
            )
        }
    }
    
    // ===== 发送消息函数 =====
    // 这个函数处理消息发送逻辑：
    // 1. 添加用户消息到列表
    // 2. 清空输入框
    // 3. 模拟LLM回复（或实际调用AI服务）
    // 4. 添加AI回复到列表
    // 注意：不跳转页面，像微信聊天一样
    val sendMessage = {
        if (promptText.isNotBlank() || currentImageUri != null) {
            // ===== 步骤1：添加用户消息到列表 =====
            val userMessage = ChatMessage(
                content = promptText,
                isUser = true,
                imageUri = currentImageUri
            )
            messages = messages + userMessage
            
            // ===== 步骤2：清空输入框 =====
            val currentPrompt = promptText
            val currentUri = currentImageUri
            promptText = ""
            currentImageUri = null
            currentImageBitmap = null
            
            // ===== 步骤3：调用回调处理业务逻辑 =====
            // 这里可以实际调用AI服务
            onSubmitPrompt(currentPrompt, currentUri)
            
            // ===== 步骤4：模拟LLM回复 =====
            // 注意：这里只是模拟回复，实际应该调用AI服务
            coroutineScope.launch {
                // 模拟AI处理时间
                delay(1000)
                
                // 模拟AI回复
                // 实际应用中应该调用真实的AI服务
                val aiReply = if (currentPrompt.contains("朗读") || currentPrompt.contains("读")) {
                    "好的，我来帮您朗读这段内容。请点击下方的播放按钮开始收听。"
                } else if (currentPrompt.contains("练习") || currentPrompt.contains("学习")) {
                    "好的，让我们开始练习！请准备好后点击开始按钮。"
                } else {
                    "收到您的消息了。如果您有图片或文档，我可以帮您进行英语阅读练习。"
                }
                
                // ===== 步骤5：添加AI回复到列表 =====
                val aiMessage = ChatMessage(
                    content = aiReply,
                    isUser = false
                )
                messages = messages + aiMessage
            }
        }
    }
    
    // ===== UI布局 =====
    Scaffold(
        topBar = {
            TopAppBar(
                title = { 
                    Text(
                        text = "练习页面",
                        fontWeight = FontWeight.Bold
                    ) 
                },
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
            // ===== 聊天列表区域 =====
            LazyColumn(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
                contentPadding = PaddingValues(vertical = 16.dp),
                reverseLayout = false
            ) {
                items(messages) { message ->
                    ChatMessageItem(message = message)
                }
                
                // 空状态提示
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
                                    text = "👋 开始新的练习",
                                    style = MaterialTheme.typography.headlineSmall,
                                    fontWeight = FontWeight.Bold
                                )
                                Spacer(modifier = Modifier.height(8.dp))
                                Text(
                                    text = "上传图片或文档开始英语阅读练习",
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
                                
                                // 删除图片按钮
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
                            // ===== 附件按钮 =====
                            // 点击此按钮才显示附件上传弹窗（拍照/相册/文档）
                            IconButton(
                                onClick = {
                                    showAttachDialog = true
                                },
                                modifier = Modifier.size(48.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.AttachFile,
                                    contentDescription = "添加附件",
                                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                            
                            // ===== 文本输入框 =====
                            TextField(
                                value = promptText,
                                onValueChange = { promptText = it },
                                placeholder = { 
                                    if (isRecording) {
                                        Text("🎤 正在聆听...")
                                    } else {
                                        Text("输入消息或上传附件")
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
                                enabled = !isRecording
                            )
                            
                            // ===== 录音按钮 =====
                            IconButton(
                                onClick = {
                                    if (!hasRecordPermission) {
                                        activity?.let {
                                            PermissionHelper.checkAndRequestRecordAudioPermission(it)
                                        }
                                    } else {
                                        if (isRecording) {
                                            speechRecognizerHelper.stopRecordingAndRecognize()
                                        } else {
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
                            
                            // ===== 发送按钮 =====
                            // 点击后不跳转页面，像微信聊天一样
                            IconButton(
                                onClick = {
                                    sendMessage()
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
                        }
                    }
                }
            }
        }
    }
    
    // ===== 附件选择对话框 =====
    // 只有点击附件按钮时才显示此对话框
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
                // TODO: 相册选择功能待实现
            },
            onDocumentClick = {
                onNavigateToDocument()
            }
        )
    }
    
    // ===== 权限授权失败提示对话框 =====
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
 * 附件选择对话框
 * 
 * 提供三种附件来源选择：
 * - 拍照：启动相机拍照
 * - 相册：从相册选择图片
 * - 文档：上传Word/PDF文档
 */
@Composable
fun AttachFileDialog(
    onDismiss: () -> Unit,
    onCameraClick: () -> Unit,
    onGalleryClick: () -> Unit,
    onDocumentClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("选择附件来源") },
        text = {
            Column(
                modifier = modifier.fillMaxWidth()
            ) {
                // ===== 拍照选项 =====
                TextButton(
                    onClick = {
                        onDismiss()
                        onCameraClick()
                    },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Start,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(
                            imageVector = Icons.Default.PhotoCamera,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary
                        )
                        Spacer(modifier = Modifier.width(16.dp))
                        Text("拍照")
                    }
                }
                
                // ===== 相册选项 =====
                TextButton(
                    onClick = {
                        onDismiss()
                        onGalleryClick()
                    },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Start,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(
                            imageVector = Icons.Default.PhotoLibrary,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary
                        )
                        Spacer(modifier = Modifier.width(16.dp))
                        Text("相册")
                    }
                }
                
                // ===== 文档选项 =====
                TextButton(
                    onClick = {
                        onDismiss()
                        onDocumentClick()
                    },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Start,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(
                            imageVector = Icons.Default.Description,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary
                        )
                        Spacer(modifier = Modifier.width(16.dp))
                        Text("文档")
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("取消")
            }
        }
    )
}
