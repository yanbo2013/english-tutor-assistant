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
import com.example.myapplication.MyApp
import com.example.myapplication.data.local.mapper.toDomain
import com.example.myapplication.domain.model.Session
import com.example.myapplication.ui.components.ChatListItem
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.map

/**
 * 首页
 *
 * 功能说明：
 * 1. 有历史 session 时：显示从 Room 数据库加载的历史 session 列表
 * 2. 无历史 session 时：显示引导页面，提示用户开始新会话
 * 3. 导航栏右上角：有历史 session 时显示"新建会话"按钮
 *
 * 数据源：SessionDao.getAllSessions() 返回 Flow，自动观察数据库变化
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    onNavigateToSettings: () -> Unit,
    onNavigateToNewChat: () -> Unit,
    onSessionSelected: (Long) -> Unit
) {
    val context = LocalContext.current
    // 从 MyApp 单例获取数据库
    val sessionDao = remember { MyApp.instance.database.sessionDao() }

    // ===== 观察数据库 Flow =====
    // Room 返回的 Flow 会自动在数据变化时重新发射
    val sessions by produceState<List<Session>>(initialValue = emptyList(), sessionDao) {
        sessionDao.getAllSessions()
            .map { entityList -> entityList.map { it.toDomain() } }
            .collectLatest { value = it }
    }

    // ===== 标志位 =====
    val hasSessions = sessions.isNotEmpty()

    // ===== 页面首次启动时的逻辑 =====
    LaunchedEffect(Unit) {
        // 预留：如果没有历史 session，可以选择直接跳转到新会话页面
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
                    if (hasSessions) {
                        IconButton(onClick = {
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
                // ===== 无历史 session 时的引导页面 =====
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
                // ===== 有历史 session 时的会话列表 =====
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
        }
    }
}
