package com.example.myapplication.ui.screens.chat

import android.Manifest
import android.app.Activity
import android.content.ContentValues
import android.graphics.Bitmap
import android.net.Uri
import android.os.Build
import android.provider.MediaStore
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
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.example.myapplication.ui.components.ChatMessage
import com.example.myapplication.ui.components.ChatMessageItem
import com.example.myapplication.utils.PermissionHelper
import com.example.myapplication.utils.SpeechRecognizerHelper
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

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
    
    // ===== 键盘控制器 =====
    // 用于控制软键盘的显示和隐藏
    // 发送消息后需要收起键盘
    val keyboardController = LocalSoftwareKeyboardController.current
    
    // ===== 状态变量 =====
    var promptText by remember { mutableStateOf("") }
    var currentImageUri by remember { mutableStateOf<Uri?>(null) }
    var currentImageBitmap by remember { mutableStateOf<Bitmap?>(null) }
    // ===== 文档状态变量 =====
    // 用于存储当前选中的文档信息
    var currentDocumentUri by remember { mutableStateOf<Uri?>(null) }
    var currentDocumentName by remember { mutableStateOf<String?>(null) }
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
    
    // ===== 图片选择启动器 =====
    // 用于从相册选择图片
    // 选择后不跳转页面，直接在当前页面显示缩略图
    val imagePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let {
            // ===== 选择图片后，直接在当前页面显示缩略图 =====
            // 不跳转页面，与发送普通消息流程相同
            currentImageUri = it
            currentImageBitmap = null
        }
    }
    
    // ===== 拍照启动器 =====
    // 用于直接启动相机拍照
    // 使用 ActivityResultContracts.TakePicture() 直接启动系统相机
    // 拍照后不跳转页面，直接在当前页面显示缩略图
    // 使用 rememberSaveable 保存 Uri 的字符串形式，确保在 Activity 重建时状态不丢失
    var cameraImageUriString by rememberSaveable { mutableStateOf<String?>(null) }
    val takePictureLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicture()
    ) { success: Boolean ->
        if (success) {
            // 拍照成功，使用之前保存的 Uri
            cameraImageUriString?.let { uriString ->
                val uri = Uri.parse(uriString)
                
                // ===== 在 Android 10+ 上更新 IS_PENDING 标志 =====
                // 确保图片可以被其他应用访问
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                    val contentValues = ContentValues().apply {
                        put(MediaStore.Images.Media.IS_PENDING, 0)
                    }
                    context.contentResolver.update(uri, contentValues, null, null)
                }
                
                currentImageUri = uri
                currentImageBitmap = null
            }
        }
        // 无论成功与否，清空 cameraImageUriString
        cameraImageUriString = null
    }
    
    // ===== 相机权限请求启动器 =====
    // 用于请求相机权限，权限授权后自动启动相机
    // 使用 rememberSaveable 保存待启动的参数，确保 Activity 重建后状态不丢失
    var pendingCameraUriString by rememberSaveable { mutableStateOf<String?>(null) }
    val cameraPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        val allGranted = permissions.values.all { it }
        if (allGranted) {
            // 权限授权成功，自动启动相机
            pendingCameraUriString?.let { uriString ->
                val uri = Uri.parse(uriString)
                // 保存到 cameraImageUriString 用于拍照回调
                cameraImageUriString = uriString
                takePictureLauncher.launch(uri)
            }
        } else {
            // 权限被拒绝，清空待启动参数
            pendingCameraUriString = null
            // 可以显示一个提示
            coroutineScope.launch {
                snackbarHostState.showSnackbar("需要相机权限才能拍照")
            }
        }
    }
    
    // ===== 文档选择启动器 =====
    // 用于选择 PDF、Word 等文档
    // 选择后不跳转页面，直接在当前页面显示文档信息
    val documentPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument()
    ) { uri: Uri? ->
        uri?.let {
            // ===== 选择文档后，直接在当前页面显示文档信息 =====
            // 不跳转页面，与发送普通消息流程相同
            currentDocumentUri = it
            // 从 URI 获取文件名
            currentDocumentName = it.lastPathSegment ?: "document"
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
    // 2. 清空输入框和附件状态
    // 3. 收起软键盘
    // 4. 调用回调处理业务逻辑
    // 5. 模拟LLM回复（或实际调用AI服务）
    // 6. 添加AI回复到列表
    // 注意：不跳转页面，像微信聊天一样
    // 支持三种消息类型：纯文本、图片、文档
    val sendMessage = {
        if (promptText.isNotBlank() || currentImageUri != null || currentDocumentUri != null) {
            // ===== 构建消息内容 =====
            // 如果有文档，在消息内容中显示文档名
            val messageContent = if (currentDocumentName != null && promptText.isBlank()) {
                "[文档] $currentDocumentName"
            } else if (currentDocumentName != null) {
                "$promptText\n[文档] $currentDocumentName"
            } else {
                promptText
            }
            
            // ===== 步骤1：添加用户消息到列表 =====
            val userMessage = ChatMessage(
                content = messageContent,
                isUser = true,
                imageUri = currentImageUri
            )
            messages = messages + userMessage
            
            // ===== 步骤2：保存当前状态并清空输入 =====
            val currentPrompt = promptText
            val currentUri = currentImageUri
            val currentDocUri = currentDocumentUri
            val currentDocName = currentDocumentName
            
            // 清空所有输入状态
            promptText = ""
            currentImageUri = null
            currentImageBitmap = null
            currentDocumentUri = null
            currentDocumentName = null
            
            // ===== 步骤3：收起软键盘 =====
            // 发送消息后收起键盘，提供更好的用户体验
            // 类似微信等聊天应用的行为
            keyboardController?.hide()
            
            // ===== 步骤4：调用回调处理业务逻辑 =====
            // 这里可以实际调用AI服务
            onSubmitPrompt(currentPrompt, currentUri)
            
            // ===== 步骤5：模拟LLM回复 =====
            // 注意：这里只是模拟回复，实际应该调用AI服务
            coroutineScope.launch {
                // 模拟AI处理时间
                delay(1000)
                
                // ===== 模拟AI回复 =====
                // 根据消息类型生成不同的回复
                val aiReply = when {
                    // 有文档的情况
                    currentDocUri != null -> {
                        "收到您上传的文档「$currentDocName」。我正在解析文档内容，请稍候..."
                    }
                    // 有图片的情况
                    currentUri != null -> {
                        "收到您上传的图片。我正在进行OCR识别，请稍候..."
                    }
                    // 纯文本的情况
                    currentPrompt.contains("朗读") || currentPrompt.contains("读") -> {
                        "好的，我来帮您朗读这段内容。请点击下方的播放按钮开始收听。"
                    }
                    currentPrompt.contains("练习") || currentPrompt.contains("学习") -> {
                        "好的，让我们开始练习！请准备好后点击开始按钮。"
                    }
                    else -> {
                        "收到您的消息了。如果您有图片或文档，我可以帮您进行英语阅读练习。"
                    }
                }
                
                // ===== 步骤6：添加AI回复到列表 =====
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
                    
                    // ===== 显示文档信息（如果有） =====
                    // 选择文档后，在输入框上侧展示文档信息
                    // 类似图片缩略图的展示方式
                    if (currentDocumentUri != null && currentDocumentName != null) {
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp),
                            colors = CardDefaults.cardColors(
                                containerColor = MaterialTheme.colorScheme.surfaceVariant
                            )
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(12.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                // 文档图标
                                Icon(
                                    imageVector = Icons.Default.Description,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(40.dp)
                                )
                                
                                Spacer(modifier = Modifier.width(12.dp))
                                
                                // 文档名称
                                Column(
                                    modifier = Modifier.weight(1f)
                                ) {
                                    Text(
                                        text = currentDocumentName ?: "文档",
                                        style = MaterialTheme.typography.bodyLarge,
                                        fontWeight = FontWeight.Medium
                                    )
                                    Text(
                                        text = "文档附件",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                                
                                // 删除文档按钮
                                IconButton(
                                    onClick = {
                                        currentDocumentUri = null
                                        currentDocumentName = null
                                    },
                                    modifier = Modifier.size(24.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Delete,
                                        contentDescription = "删除文档",
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
                            // 支持三种消息类型：纯文本、图片、文档
                            IconButton(
                                onClick = {
                                    sendMessage()
                                },
                                enabled = promptText.isNotBlank() || currentImageUri != null || currentDocumentUri != null
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Send,
                                    contentDescription = "发送",
                                    tint = if (promptText.isNotBlank() || currentImageUri != null || currentDocumentUri != null) {
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
    // 选择后不跳转页面，直接在当前页面显示缩略图或文档信息
    if (showAttachDialog) {
        AttachFileDialog(
            onDismiss = { showAttachDialog = false },
            onCameraClick = {
                // ===== 拍照功能 =====
                // 使用 ActivityResultContracts.TakePicture() 直接启动系统相机
                // 不跳转页面，与发送普通消息流程相同
                activity?.let {
                    // ===== 先关闭对话框 =====
                    showAttachDialog = false
                    
                    // ===== 获取所有相机权限 =====
                    // 包含相机权限和存储权限（根据 Android 版本不同）
                    val cameraPermissions = PermissionHelper.getCameraPermissions()
                    
                    // ===== 检查是否已有所有权限 =====
                    val hasAllPermissions = PermissionHelper.hasPermissions(it, cameraPermissions)
                    
                    if (hasAllPermissions) {
                        // ===== 已有权限，直接启动相机 =====
                        
                        // 创建图片文件名
                        val timeStamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
                        val imageName = "IMG_$timeStamp.jpg"
                        
                        // 创建 ContentValues 用于保存图片
                        val contentValues = ContentValues().apply {
                            put(MediaStore.Images.Media.DISPLAY_NAME, imageName)
                            put(MediaStore.Images.Media.MIME_TYPE, "image/jpeg")
                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                                put(MediaStore.Images.Media.RELATIVE_PATH, "Pictures/EnglishReading")
                                put(MediaStore.Images.Media.IS_PENDING, 1)
                            }
                        }
                        
                        // 创建图片 Uri
                        val uri = it.contentResolver.insert(
                            MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                            contentValues
                        )
                        
                        uri?.let { imageUri ->
                            // 保存 Uri 的字符串形式用于后续使用
                            // 使用字符串形式更可靠，避免 Parcelable 在某些情况下的问题
                            cameraImageUriString = imageUri.toString()
                            // 启动相机
                            takePictureLauncher.launch(imageUri)
                        }
                    } else {
                        // ===== 没有权限，先创建图片 Uri 并保存，然后请求权限 =====
                        // 权限授权后会自动启动相机
                        
                        // 创建图片文件名
                        val timeStamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
                        val imageName = "IMG_$timeStamp.jpg"
                        
                        // 创建 ContentValues 用于保存图片
                        val contentValues = ContentValues().apply {
                            put(MediaStore.Images.Media.DISPLAY_NAME, imageName)
                            put(MediaStore.Images.Media.MIME_TYPE, "image/jpeg")
                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                                put(MediaStore.Images.Media.RELATIVE_PATH, "Pictures/EnglishReading")
                                put(MediaStore.Images.Media.IS_PENDING, 1)
                            }
                        }
                        
                        // 创建图片 Uri
                        val uri = it.contentResolver.insert(
                            MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                            contentValues
                        )
                        
                        uri?.let { imageUri ->
                            // 保存 Uri 到 pendingCameraUriString，权限授权后使用
                            pendingCameraUriString = imageUri.toString()
                            // 请求相机权限
                            // 权限授权后，cameraPermissionLauncher 会自动启动相机
                            cameraPermissionLauncher.launch(cameraPermissions)
                        }
                    }
                }
            },
            onGalleryClick = {
                // ===== 相册选择功能 =====
                // 使用 ActivityResultContracts.GetContent 启动系统图片选择器
                // 用户可以从相册选择图片
                // 选择后不跳转页面，直接在当前页面显示缩略图
                // 与发送普通消息流程相同
                // ===== 先关闭对话框 =====
                showAttachDialog = false
                imagePickerLauncher.launch("image/*")
            },
            onDocumentClick = {
                // ===== 文档选择功能 =====
                // 使用 ActivityResultContracts.OpenDocument 启动系统文档选择器
                // 支持 PDF、Word、PowerPoint 等文档
                // 选择后不跳转页面，直接在当前页面显示文档信息
                // 与发送普通消息流程相同
                // ===== 先关闭对话框 =====
                showAttachDialog = false
                documentPickerLauncher.launch(
                    arrayOf(
                        "application/pdf",
                        "application/vnd.openxmlformats-officedocument.wordprocessingml.document",
                        "application/msword",
                        "application/vnd.openxmlformats-officedocument.presentationml.presentation",
                        "application/vnd.ms-powerpoint"
                    )
                )
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
