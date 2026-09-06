# English Tutor Assistant

> 📚 **AI 英语辅导助手** —— 基于 Jetpack Compose 的 Android 英语朗读练习应用

## 项目简介

English Tutor Assistant 是一款面向英语学习者的 AI 辅导应用。用户可以通过**拍照**或**上传文档**的方式快速获取英语文本内容，结合 **OCR 文字识别**、**AI 语音合成**、**智能发音评测**等能力，打造沉浸式的英语朗读练习体验。

### 核心价值

- 📷 **拍照即练** —— 拍摄任意英文材料即可开始练习
- 📄 **文档解析** —— 支持 PDF / Word / PPT 多格式文档导入
- 🎙️ **语音识别** —— 基于百度语音 API 的中英文混合识别
- 📊 **智能评测** —— 准确度、流利度、完整度三维评分
- 💬 **会话式交互** —— 聊天式 UI，自然流畅的学习体验

---

## ✨ 功能特性

### 已实现

| 模块 | 功能 | 说明 |
|------|------|------|
| 🖼️ **相机拍照** | CameraX 集成 | 实时拍照获取图片，支持权限自适应 |
| 📝 **OCR 识别** | ML Kit 中英文识别 | 自动提取图片中的英文文本 |
| 📑 **文档解析** | PDF / DOCX / PPTX | Apache POI + PdfBox 多格式支持 |
| 🎤 **语音录制** | MediaRecorder | AMR 格式录音，自动管理临时文件 |
| 🔊 **语音识别** | 百度语音 REST API | 短语音识别，JSON 协议，Token 自动缓存 |
| 🏠 **聊天会话** | NewChatScreen | 微信式消息列表，模拟 AI 回复 |
| 📚 **历史记录** | Room 本地持久化 | Session + Record 二级数据模型 |
| ⚙️ **设备标识** | ANDROID_ID + UUID | 自动降级策略，百度 CUID 友好 |

### 规划中

- [ ] 真实 AI 服务对接（DeepSeek 等 LLM）
- [ ] 讯飞语音评测集成（发音打分）
- [ ] TTS 语音合成（AI 朗读示范）
- [ ] 相册图片选择
- [ ] 学习统计与数据可视化
- [ ] 单词本 / 生词记录

---

## 🏗️ 技术架构

### 架构模式

项目采用 **Clean Architecture（清洁架构）** 分层设计：

```
┌─────────────────────────────────────────┐
│              UI Layer (ui/)              │  ← 声明式界面 + 导航
│  ┌─────────┐ ┌─────────┐ ┌─────────┐   │
│  │ Screens │ │Navigation│ │Components│   │
│  └─────────┘ └─────────┘ └─────────┘   │
├─────────────────────────────────────────┤
│           Domain Layer (domain/)         │  ← 业务模型 + 仓库接口
│  ┌─────────┐ ┌─────────────────────┐     │
│  │ Models  │ │ Repository Interfaces│     │
│  └─────────┘ └─────────────────────┘     │
├─────────────────────────────────────────┤
│            Data Layer (data/)            │  ← 本地数据库 + 远程 API
│  ┌─────────────┐ ┌─────────────────┐     │
│  │ Local (Room)│ │ Remote (Network)│     │
│  └─────────────┘ └─────────────────┘     │
├─────────────────────────────────────────┤
│           Network Layer (network/)       │  ← Retrofit API 定义
├─────────────────────────────────────────┤
│             Utils (utils/)               │  ← 工具类与 Helper
└─────────────────────────────────────────┘
```

### 技术栈

| 类别 | 技术 | 版本 | 用途 |
|------|------|------|------|
| **语言** | Kotlin | 2.2.10 | 开发语言 |
| **UI 框架** | Jetpack Compose | BOM 2024.09.00 | 声明式界面 |
| **Material** | Material 3 | - | 设计系统 |
| **导航** | Navigation Compose | 2.7.6 | 页面路由 |
| **本地存储** | Room Database | 2.6.1 | SQLite ORM |
| **数据存储** | DataStore | 1.0.0 | 轻量键值存储 |
| **网络请求** | Retrofit + OkHttp | 2.9.0 / 4.12.0 | HTTP 客户端 |
| **异步** | Kotlin Coroutines | 1.7.3 | 协程并发 |
| **相机** | CameraX | 1.3.1 | 拍照功能 |
| **OCR** | Google ML Kit | 16.0.0 | 中英文文字识别 |
| **语音** | 百度语音 API | - | 语音识别 REST 接口 |
| **音频** | Media3 ExoPlayer | 1.2.1 | 音频播放 |
| **图片** | Coil | 2.5.0 | Compose 图片加载 |
| **文档** | Apache POI | 5.2.5 | Word / PPT 解析 |
| **PDF** | PdfBox Android | 2.0.27.0 | PDF 解析 |
| **JSON** | Gson | 2.10.1 | 序列化 |
| **构建** | AGP / Gradle | 9.1.0 | Android 构建 |

---

## 📱 页面结构

| 页面 | 路由 | 文件 | 说明 |
|------|------|------|------|
| 🏠 **Home** | `home` | `ui/screens/home/HomeScreen.kt` | 首页，功能入口 |
| 💬 **NewChat** | `new_chat` | `ui/screens/chat/NewChatScreen.kt` | 聊天会话页（主练习入口） |
| 📷 **Camera** | `camera` | `ui/screens/camera/CameraScreen.kt` | 相机拍照页 |
| 🖼️ **ImageChat** | `image_chat/{imageUri}` | `ui/screens/chat/ImageChatScreen.kt` | 图片聊天页 |
| 📄 **DocumentUpload** | `document_upload` | `ui/screens/document/DocumentUploadScreen.kt` | 文档上传页 |
| 📖 **Practice** | `practice/{sessionId}` | `ui/screens/practice/PracticeScreen.kt` | 朗读练习页 |
| 🕐 **History** | `history` | `ui/screens/history/HistoryScreen.kt` | 历史记录页 |
| ⚙️ **Settings** | `settings` | `ui/screens/settings/SettingsScreen.kt` | 设置页 |

### 页面流程图

```
                    ┌──────────────┐
                    │  MainActivity│
                    └──────┬───────┘
                           ▼
                    ┌──────────────┐
                    │   Home 首页   │◀──── History 历史
                    └──────┬───────┘
                           │ 新建会话
                           ▼
                    ┌──────────────┐
                    │  NewChat     │
                    │  聊天会话    │
                    └──┬───────┬───┘
            拍照附件   │       │ 文档附件
                       ▼       ▼
                  ┌──────┐  ┌──────────┐
                  │Camera│  │Document  │
                  │ 拍照 │  │Upload    │
                  └──┬───┘  └────┬─────┘
                     │            │
                     ▼            ▼
                ┌──────────┐  ┌──────────┐
                │ImageChat │  │ Practice  │
                │ 图片聊天 │  │ 朗读练习 │
                └──────────┘  └──────────┘
```

---

## 📂 目录结构

```
app/src/main/java/com/example/myapplication/
│
├── MainActivity.kt                 # 入口 Activity
│
├── data/                           # 📦 数据层
│   ├── local/                      #   本地数据
│   │   ├── AppDatabase.kt          #   Room 数据库定义
│   │   ├── dao/                    #   DAO 接口
│   │   │   ├── SessionDao.kt
│   │   │   └── RecordDao.kt
│   │   └── entity/                 #   数据库实体
│   │       ├── SessionEntity.kt
│   │       └── RecordEntity.kt
│   └── remote/                     #   远程数据
│       └── model/                  #   API 响应模型
│           ├── AIResponse.kt
│           ├── OCRResponse.kt
│           ├── SpeechResponse.kt
│           └── TTSResponse.kt
│
├── domain/                         # 📦 领域层
│   ├── model/                      #   业务模型
│   │   ├── Session.kt              #   学习会话
│   │   ├── Record.kt               #   练习记录
│   │   ├── TextSegment.kt          #   文本段落
│   │   └── EvaluationResult.kt     #   发音评测结果
│   └── repository/                 #   仓库接口
│       ├── AIRepository.kt
│       ├── OCRRepository.kt
│       ├── SpeechRepository.kt
│       ├── EvaluationRepository.kt
│       └── TTSRepository.kt
│
├── network/                        # 📦 网络层
│   ├── BaiduSpeechApi.kt           #   百度语音 API 接口
│   └── RetrofitClient.kt           #   Retrofit 单例
│
├── ui/                             # 📦 UI 层
│   ├── navigation/                 #   导航
│   │   ├── Screen.kt               #   路由定义
│   │   └── AppNavGraph.kt          #   导航图
│   ├── screens/                    #   页面
│   │   ├── home/
│   │   ├── chat/                   #   NewChat + ImageChat
│   │   ├── camera/
│   │   ├── document/
│   │   ├── practice/
│   │   ├── history/
│   │   └── settings/
│   ├── components/                 #   通用组件
│   │   ├── ChatMessage.kt
│   │   ├── ChatInputBar.kt
│   │   ├── ChatListItem.kt
│   │   └── PermissionHandler.kt
│   └── theme/                      #   主题
│       ├── Color.kt
│       ├── Theme.kt
│       └── Type.kt
│
└── utils/                          # 📦 工具类
    ├── AudioHelper.kt              #   录音辅助
    ├── SpeechRecognizerHelper.kt   #   百度语音识别
    ├── DocumentParser.kt           #   PDF/DOCX/PPTX 解析
    ├── DeviceIdGenerator.kt        #   设备唯一标识
    ├── PermissionHelper.kt         #   权限管理
    └── Constants.kt                #   常量定义
```

---

## 🗄️ 数据模型

### 领域模型

```kotlin
// 学习会话
data class Session(
    val id: Long = 0,
    val timestamp: Long = System.currentTimeMillis(),
    val imageUrl: String?,           // 来源图片 URL
    val recognizedText: String,      // OCR 识别文本
    val segments: List<TextSegment>, // 分段文本
    val sessionType: String = "practice",
    val completedAt: Long? = null
)

// 朗读练习记录
data class Record(
    val id: Long = 0,
    val sessionId: Long,
    val sentenceIndex: Int,
    val originalText: String,
    val studentAudioPath: String?,
    val recognizedText: String?,
    val evaluationResult: EvaluationResult?,
    val createdAt: Long = System.currentTimeMillis()
)

// 发音评测结果
data class EvaluationResult(
    val accuracyScore: Float,        // 准确度 0-100
    val fluencyScore: Float,         // 流利度 0-100
    val completenessScore: Float,    // 完整度 0-100
    val overallScore: Float,         // 综合评分
    val feedback: String,            // 改进建议
    val wordDetails: List<WordEvaluation>? = null  // 逐词评测
)
```

### 数据库设计

- **数据库名称**: `english_reading_db`
- **ORM**: Room Database
- **版本**: 1

---

## 🔧 环境配置

### 系统要求

| 项目 | 要求 |
|------|------|
| **Android Studio** | Hedgehog (2023.1.1) 或更高 |
| **JDK** | 11+ |
| **compileSdk** | 36 |
| **minSdk** | 26 (Android 8.0) |
| **targetSdk** | 36 |
| **AGP** | 9.1.0 |
| **Kotlin** | 2.2.10 |

### 百度语音 API 配置

项目使用百度语音识别服务，需要配置以下密钥：

1. 在 [百度 AI 开放平台](https://ai.baidu.com/tech/speech) 申请语音识别应用
2. 在项目根目录 `local.properties` 中添加配置：

```properties
# local.properties（不要提交到 Git）
BAIDU_SPEECH_APP_ID=你的AppID
BAIDU_SPEECH_API_KEY=你的APIKey
BAIDU_SPEECH_SECRET_KEY=你的SecretKey
```

> ⚠️ 这些密钥通过 `build.gradle.kts` 读取并注入到 `BuildConfig` 中，**请勿硬编码到源码或提交到公开仓库**。

### 其他 API 密钥

[Constants.kt](app/src/main/java/com/example/myapplication/utils/Constants.kt) 中预留了以下服务的占位符（当前为开发预留，尚未集成）：

| 服务 | 用途 | 状态 |
|------|------|------|
| 讯飞 XUNFEI | TTS / STT / 发音评测 | 🔜 规划中 |
| DeepSeek | AI 对话理解 | 🔜 规划中 |

---

## 🚀 快速开始

### 方式一：Android Studio

1. 克隆仓库：
   ```bash
   git clone https://github.com/yanbo2013/english-tutor-assistant.git
   ```

2. 使用 Android Studio 打开项目目录

3. 等待 Gradle 同步完成

4. 在 `local.properties` 中配置百度语音 API 密钥（可选，语音识别功能需要）

5. 连接 Android 设备或启动模拟器，点击 **Run** 按钮

### 方式二：命令行构建

项目提供了便捷的构建脚本 `build.sh`：

```bash
# 编译调试版本
./build.sh debug

# 编译发布版本
./build.sh release

# 安装调试版本到连接的设备
./build.sh install

# 运行单元测试
./build.sh test

# 清理构建
./build.sh clean
```

或者直接使用 Gradle Wrapper：

```bash
./gradlew assembleDebug
./gradlew assembleRelease
./gradlew installDebug
```

---

## 🔑 权限说明

应用需要以下 Android 运行时权限：

| 权限 | 用途 | 必需 |
|------|------|------|
| `CAMERA` | 拍照获取英语材料 | 可选（不使用拍照功能时可拒绝） |
| `RECORD_AUDIO` | 朗读练习录音 | 可选 |
| `INTERNET` | 调用百度语音识别 API | 必需 |
| `ACCESS_NETWORK_STATE` | 网络状态检测 | 必需 |
| `READ_MEDIA_IMAGES` (Android 13+) | 相册图片访问 | 可选 |
| `READ_EXTERNAL_STORAGE` (Android 12-) | 外部存储读取 | 可选 |

---

## 🏛️ 核心实现详解

### 1. 百度语音识别流程

```
用户录音 (MediaRecorder → AMR 文件)
         │
         ▼
  Base64 编码音频数据
         │
         ▼
  获取 Access Token（自动缓存 + 过期刷新）
         │
         ▼
  POST https://vop.baidu.com/server_api
  JSON Body: { format, rate, channel, cuid, dev_pid, speech, len }
         │
         ▼
  处理响应：err_no == 0 → 返回识别文本
```

### 2. 设备唯一标识生成

`DeviceIdGenerator` 实现了多级降级策略：

```
尝试 ANDROID_ID（Settings.Secure.ANDROID_ID）
    │
    ├─ 有效 → 返回
    │
    └─ 无效 → 生成随机 UUID v4
```

- 带内存缓存，应用生命周期内只生成一次
- 自动检测已知的 ANDROID_ID Bug 值 (`9774d56d682e549c`)
- 支持业务前缀（如 `baidu_speech-xxx`）

### 3. 文档解析支持

| 格式 | 库 | 说明 |
|------|------|------|
| `.pdf` | PdfBox Android | 纯文本提取 |
| `.docx` | Apache POI | 段落文本拼接 |
| `.pptx` | Apache POI | 幻灯片形状文本提取 |

### 4. 页面导航策略

- **拍照流程**：`CameraScreen` 拍照后使用 `popUpTo { inclusive = true }` 从页面栈移除
- **消息发送**：`NewChatScreen` 发送消息后**不跳转**，保持在聊天页面，模拟 AI 回复

---

## 📝 版本历史

| 版本 | 日期 | 说明 |
|------|------|------|
| v3.2.0 | 2026-04-21 | 🐛 修复发送消息后键盘不收起的问题 |
| v3.1.0 | 2026-04-21 | 🐛 彻底修复 onSubmitPrompt 导致页面跳转 |
| v3.0.0 | 2026-04-21 | 🐛 修复消息发送跳转 + 相机页面栈问题 |
| v2.0.0 | 2026-04-21 | ✨ 首页重构为聊天会话式 UI |
| v1.0.0 | 2026-04-18 | ✨ CUID 设备标识优化 |

---

## 📚 相关文档

项目根目录包含以下详细技术文档：

- [变更记录.md](变更记录.md) — 详细的版本修复记录
- [项目架构分析报告.md](项目架构分析报告.md) — 深度架构分析
- [百度语音识别配置完成.md](百度语音识别配置完成.md) — 语音识别对接笔记
- [CUID设备标识优化变更记录.md](CUID设备标识优化变更记录.md) — 设备标识方案演进
- [首页重构-聊天会话式UI变更记录.md](首页重构-聊天会话式UI变更记录.md) — UI 重构过程

---

## 🤝 贡献

欢迎提交 Issue 和 Pull Request！

---

## 📄 License

本项目基于 MIT License 开源。
