package com.example.myapplication.ui.navigation

/**
 * 应用屏幕路由
 */
sealed class Screen(val route: String) {
    object Home : Screen("home")
    object Camera : Screen("camera")
    object DocumentUpload : Screen("document_upload")
    object ImageChat : Screen("image_chat/{imageUri}") {
        /**
         * 创建图片聊天页面的路由地址
         *
         * @param imageUri 图片的URI字符串，用于在页面间传递图片资源
         * @return 包含图片URI的完整路由字符串，格式为 "image_chat/{imageUri}"
         */
        fun createRoute(imageUri: String): String {
            // 对 URI 进行编码，避免特殊字符导致导航失败
            val encodedUri = java.net.URLEncoder.encode(imageUri, "UTF-8")
            return "image_chat/$encodedUri"
        }
    }
    object Practice : Screen("practice/{sessionId}") {
        /**
         * 创建练习页面的路由地址
         *
         * @param sessionId 学习会话的唯一标识ID
         * @return 包含会话ID的完整路由字符串，格式为 "practice/{sessionId}"
         */
        fun createRoute(sessionId: Long) = "practice/$sessionId"
    }
    object History : Screen("history")
    object Settings : Screen("settings")
    
    /**
     * 新会话聊天页面
     * 
     * 用于创建新的聊天会话，包含底部输入栏和附件上传功能
     */
    object NewChat : Screen("new_chat")
}
