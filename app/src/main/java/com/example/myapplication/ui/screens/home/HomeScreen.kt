package com.example.myapplication.ui.screens.home

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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.myapplication.domain.model.Session
import com.example.myapplication.domain.model.TextSegment
import com.example.myapplication.ui.components.ChatListItem

/**
 * 首页
 * 
 * 功能说明：
 * 1. 有历史session时：显示历史session列表，不显示底部输入框
 * 2. 无历史session时：显示引导页面，提示用户开始新会话
 * 3. 导航栏右上角：有历史session时显示"新建会话"按钮
 * 4. 点击新建会话按钮：直接跳转到新会话页面，不显示附件上传弹窗
 * 
 * 注意：
 * - 附件上传弹窗只在新会话页面（NewChatScreen）中点击附件按钮时才显示
 * - 首页不显示底部输入框
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    onNavigateToSettings: () -> Unit,
    onNavigateToNewChat: () -> Unit,
    onSessionSelected: (Long) -> Unit
) {
    // ===== 状态变量 =====
    // TODO: 未来需要从数据库获取真实的session列表
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
    
    // ===== 标志位 =====
    // 是否有历史session
    val hasSessions = sessions.isNotEmpty()
    
    // ===== 页面首次启动时的逻辑 =====
    // 无历史session时，可以选择直接跳转到新会话页面
    // 这里选择显示引导页面，让用户更清楚当前状态
    LaunchedEffect(Unit) {
        // 如果没有历史session，可以选择直接跳转到新会话页面
        // if (!hasSessions) {
        //     onNavigateToNewChat()
        // }
    }
    
    // ===== UI布局 =====
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
                    // ===== 新建会话按钮 =====
                    // 只有当有历史session时才显示这个按钮
                    // 点击此按钮直接跳转到新会话页面，不显示附件上传弹窗
                    if (hasSessions) {
                        IconButton(onClick = {
                            // 直接跳转到新会话页面
                            onNavigateToNewChat()
                        }) {
                            Icon(
                                imageVector = Icons.Default.Add,
                                contentDescription = "新建会话"
                            )
                        }
                    }
                    
                    // ===== 设置按钮 =====
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
            // ===== 内容区域 =====
            if (!hasSessions) {
                // ===== 无历史session时的引导页面 =====
                // 显示引导文字，提示用户开始新会话
                // 注意：首页不显示底部输入框
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
                        Spacer(modifier = Modifier.height(24.dp))
                        // ===== 开始新会话按钮 =====
                        Button(
                            onClick = {
                                onNavigateToNewChat()
                            }
                        ) {
                            Icon(
                                imageVector = Icons.Default.Add,
                                contentDescription = null
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("开始新会话")
                        }
                    }
                }
            } else {
                // ===== 有历史session时的会话列表 =====
                // 注意：不显示底部输入框
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
                        // ===== 会话列表项 =====
                        ChatListItem(
                            session = session,
                            onClick = {
                                // 点击会话项跳转到练习页面
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
            
            // ===== 注意：首页不显示底部输入框 =====
            // 底部输入框只在新会话页面（NewChatScreen）中显示
            // 这样设计的原因：
            // 1. 首页是历史会话列表页，不需要输入功能
            // 2. 输入功能只在新会话页面中需要
            // 3. 保持页面职责单一，避免混淆
        }
    }
}
