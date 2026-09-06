plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.compose)
    id("com.google.devtools.ksp")
}

import java.util.Properties
import java.io.FileInputStream

android {
    namespace = "com.example.myapplication"
    compileSdk {
        version = release(36) {
            minorApiLevel = 1
        }
    }

    defaultConfig {
        applicationId = "com.example.myapplication"
        minSdk = 26
        targetSdk = 36
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        
        // 读取百度语音识别配置
        val localProperties = Properties()
        val localPropertiesFile = rootProject.file("local.properties")
        if (localPropertiesFile.exists()) {
            localProperties.load(FileInputStream(localPropertiesFile))
        }
        
        buildConfigField("String", "BAIDU_SPEECH_APP_ID", 
            "\"${localProperties.getProperty("BAIDU_SPEECH_APP_ID", "")}\"")
        buildConfigField("String", "BAIDU_SPEECH_API_KEY", 
            "\"${localProperties.getProperty("BAIDU_SPEECH_API_KEY", "")}\"")
        buildConfigField("String", "BAIDU_SPEECH_SECRET_KEY", 
            "\"${localProperties.getProperty("BAIDU_SPEECH_SECRET_KEY", "")}\"")
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
    buildFeatures {
        compose = true
        buildConfig = true
    }
}

dependencies {
    // === 基础库 ===
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.activity.compose)
    
    // === Jetpack Compose ===
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.compose.ui)
    implementation(libs.androidx.compose.ui.graphics)
    implementation(libs.androidx.compose.ui.tooling.preview)
    implementation(libs.androidx.compose.material3)
    implementation("androidx.compose.material:material-icons-extended")
    
    // === 导航 ===
    implementation("androidx.navigation:navigation-compose:2.7.6")
    
    // === 相机功能 CameraX ===
    implementation("androidx.camera:camera-core:1.3.1")
    implementation("androidx.camera:camera-camera2:1.3.1")
    implementation("androidx.camera:camera-lifecycle:1.3.1")
    implementation("androidx.camera:camera-view:1.3.1")
    
    // === OCR 识别 ML Kit ===
    implementation("com.google.mlkit:text-recognition:16.0.0")
    implementation("com.google.mlkit:text-recognition-chinese:16.0.0")
    
    // === 网络请求 Retrofit ===
    implementation("com.squareup.retrofit2:retrofit:2.9.0")
    implementation("com.squareup.retrofit2:converter-gson:2.9.0")
    implementation("com.squareup.okhttp3:logging-interceptor:4.12.0")
    
    // === 本地数据库 Room ===
    implementation("androidx.room:room-runtime:2.6.1")
    implementation("androidx.room:room-ktx:2.6.1")
    ksp("androidx.room:room-compiler:2.6.1")
    
    // === 数据存储 DataStore ===
    implementation("androidx.datastore:datastore-preferences:1.0.0")
    
    // === 协程 Coroutines ===
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.7.3")
    
    // === 图片加载 Coil ===
    implementation("io.coil-kt:coil-compose:2.5.0")
    
    // === 音频播放 Media3 ===
    implementation("androidx.media3:media3-exoplayer:1.2.1")
    
    // === JSON 处理 Gson ===
    implementation("com.google.code.gson:gson:2.10.1")
    
    // === 文档解析 ===
    implementation("org.apache.poi:poi:5.2.5")
    implementation("org.apache.poi:poi-ooxml:5.2.5")
    implementation("com.tom-roush:pdfbox-android:2.0.27.0")
    
    // === 测试 ===
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.compose.ui.test.junit4)
    debugImplementation(libs.androidx.compose.ui.tooling)
    debugImplementation(libs.androidx.compose.ui.test.manifest)
}