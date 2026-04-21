package com.example.myapplication.ui.components

import android.net.Uri
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Android
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import java.util.*

/**
 * 聊天消息数据类
 * 
 * 用于表示聊天界面中的一条消息
 * 
 * @property id 消息唯一标识
 * @property content 消息文本内容
 * @property isUser 是否为用户发送的消息（true=用户，false=AI）
 * @property timestamp 消息时间戳
 * @property imageUri 消息附带的图片URI（可选）
 */
data class ChatMessage(
    val id: String = UUID.randomUUID().toString(),
    val content: String,
    val isUser: Boolean,
    val timestamp: Long = System.currentTimeMillis(),
    val imageUri: Uri? = null
)

/**
 * 聊天消息项组件
 * 
 * 根据消息类型（用户/AI）显示不同样式的消息气泡
 * 
 * 样式说明：
 * - 用户消息：右侧对齐，蓝色气泡，显示"[我]"标识
 * - AI消息：左侧对齐，灰色气泡，显示"[AI回复]"标识
 * 
 * @param message 消息数据对象
 * @param modifier 可选的修饰符
 */
@Composable
fun ChatMessageItem(
    message: ChatMessage,
    modifier: Modifier = Modifier
) {
    // ===== 根据消息类型决定对齐方式 =====
    // 用户消息右对齐，AI消息左对齐
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = if (message.isUser) Arrangement.End else Arrangement.Start,
        verticalAlignment = Alignment.Top
    ) {
        // ===== AI消息：显示头像和消息气泡 =====
        if (!message.isUser) {
            // AI头像
            MessageAvatar(
                icon = Icons.Default.Android,
                contentDescription = "AI助手",
                modifier = Modifier.padding(end = 8.dp)
            )
        }
        
        // ===== 消息气泡 =====
        Column(
            horizontalAlignment = if (message.isUser) Alignment.End else Alignment.Start
        ) {
            // ===== 发送者标识 =====
            // 显示"[我]"或"[AI回复]"
            Text(
                text = if (message.isUser) "[我]" else "[AI回复]",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                fontWeight = FontWeight.Medium,
                modifier = Modifier.padding(bottom = 4.dp)
            )
            
            // ===== 消息气泡卡片 =====
            Card(
                colors = CardDefaults.cardColors(
                    containerColor = if (message.isUser) {
                        // 用户消息：蓝色气泡
                        MaterialTheme.colorScheme.primaryContainer
                    } else {
                        // AI消息：灰色气泡
                        MaterialTheme.colorScheme.surfaceVariant
                    }
                ),
                shape = RoundedCornerShape(
                    topStart = 16.dp,
                    topEnd = 16.dp,
                    // 用户消息：右下角圆角小，左下角圆角大
                    // AI消息：左下角圆角小，右下角圆角大
                    bottomStart = if (message.isUser) 16.dp else 4.dp,
                    bottomEnd = if (message.isUser) 4.dp else 16.dp
                ),
                modifier = Modifier.widthIn(max = 280.dp)
            ) {
                Column(
                    modifier = Modifier.padding(12.dp)
                ) {
                    // ===== 显示图片（如果有） =====
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
                    
                    // ===== 显示文本内容 =====
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
        
        // ===== 用户消息：显示头像在右侧 =====
        if (message.isUser) {
            // 用户头像
            MessageAvatar(
                icon = Icons.Default.Person,
                contentDescription = "我",
                modifier = Modifier.padding(start = 8.dp)
            )
        }
    }
}

/**
 * 消息头像组件
 * 
 * 用于显示消息发送者的头像
 * 
 * @param icon 头像图标
 * @param contentDescription 内容描述（无障碍）
 * @param modifier 可选的修饰符
 */
@Composable
fun MessageAvatar(
    icon: ImageVector,
    contentDescription: String,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier.size(36.dp),
        shape = RoundedCornerShape(12.dp),
        color = MaterialTheme.colorScheme.secondaryContainer
    ) {
        Box(
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = contentDescription,
                modifier = Modifier.size(20.dp),
                tint = MaterialTheme.colorScheme.onSecondaryContainer
            )
        }
    }
}
