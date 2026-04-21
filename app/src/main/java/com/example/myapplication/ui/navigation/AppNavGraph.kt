package com.example.myapplication.ui.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.example.myapplication.ui.screens.camera.CameraScreen
import com.example.myapplication.ui.screens.chat.ImageChatScreen
import com.example.myapplication.ui.screens.chat.NewChatScreen
import com.example.myapplication.ui.screens.document.DocumentUploadScreen
import com.example.myapplication.ui.screens.history.HistoryScreen
import com.example.myapplication.ui.screens.home.HomeScreen
import com.example.myapplication.ui.screens.practice.PracticeScreen
import com.example.myapplication.ui.screens.settings.SettingsScreen

/**
 * 应用导航图
 */
@Composable
fun AppNavGraph(
    navController: NavHostController,
    startDestination: String = Screen.Home.route
) {
    NavHost(
        navController = navController,
        startDestination = startDestination
    ) {
        // 首页
        composable(Screen.Home.route) {
            HomeScreen(
                onNavigateToSettings = {
                    navController.navigate(Screen.Settings.route)
                },
                onNavigateToNewChat = {
                    navController.navigate(Screen.NewChat.route)
                },
                onSessionSelected = { sessionId ->
                    navController.navigate(Screen.Practice.createRoute(sessionId))
                }
            )
        }
        
        // 相机拍照页
        composable(Screen.Camera.route) {
            CameraScreen(
                onNavigateBack = {
                    navController.popBackStack()
                },
                onImageCaptured = { imageUri ->
                    // 拍照成功后跳转到 ImageChat 页面
                    // 使用 popUpTo 从页面栈中移除 CameraScreen
                    // 这样在 ImageChat 页面按 back 键时，不会返回到 CameraScreen
                    navController.navigate(Screen.ImageChat.createRoute(imageUri)) {
                        popUpTo(Screen.Camera.route) {
                            inclusive = true  // 包含 CameraScreen 一起移除
                        }
                    }
                }
            )
        }
        
        // 文档上传页
        composable(Screen.DocumentUpload.route) {
            DocumentUploadScreen(
                onNavigateBack = {
                    navController.popBackStack()
                },
                onDocumentUploaded = { sessionId, text ->
                    navController.navigate(Screen.Practice.createRoute(sessionId))
                }
            )
        }
        
        // 图片Chat页
        composable(
            route = Screen.ImageChat.route,
            arguments = listOf(
                navArgument("imageUri") { type = NavType.StringType }
            )
        ) { backStackEntry ->
            val encodedImageUri = backStackEntry.arguments?.getString("imageUri")
            // 解码 URI
            val imageUri = encodedImageUri?.let {
                java.net.URLDecoder.decode(it, "UTF-8")
            }?.let {
                android.net.Uri.parse(it)
            }
            
            ImageChatScreen(
                imageUri = imageUri,
                onNavigateBack = {
                    navController.popBackStack()
                },
                onSubmitPrompt = { prompt, uri ->
                    // TODO: 处理用户输入的提示词，调用AI服务
                    // 暂时跳转到练习页面
                    navController.navigate(Screen.Practice.createRoute(System.currentTimeMillis()))
                }
            )
        }
        
        // 练习页
        composable(
            route = Screen.Practice.route,
            arguments = listOf(
                navArgument("sessionId") {
                    type = NavType.LongType
                }
            )
        ) { backStackEntry ->
            val sessionId = backStackEntry.arguments?.getLong("sessionId") ?: 0L
            PracticeScreen(
                sessionId = sessionId,
                onNavigateBack = {
                    navController.popBackStack()
                }
            )
        }
        
        // 历史记录页
        composable(Screen.History.route) {
            HistoryScreen(
                onNavigateBack = {
                    navController.popBackStack()
                },
                onSessionSelected = { sessionId ->
                    navController.navigate(Screen.Practice.createRoute(sessionId))
                }
            )
        }
        
        // 设置页
        composable(Screen.Settings.route) {
            SettingsScreen(
                onNavigateBack = {
                    navController.popBackStack()
                }
            )
        }
        
        // ===== 新会话聊天页 =====
        // 点击新建session时跳转到这个页面
        // 这个页面包含底部输入栏，点击附件按钮才显示附件上传弹窗
        composable(Screen.NewChat.route) {
            NewChatScreen(
                onNavigateBack = {
                    navController.popBackStack()
                },
                onNavigateToCamera = {
                    navController.navigate(Screen.Camera.route)
                },
                onNavigateToDocument = {
                    navController.navigate(Screen.DocumentUpload.route)
                },
                onSubmitPrompt = { prompt, uri ->
                    // TODO: 处理用户输入的提示词，调用AI服务
                    // 暂时跳转到练习页面
                    navController.navigate(Screen.Practice.createRoute(System.currentTimeMillis()))
                }
            )
        }
    }
}
