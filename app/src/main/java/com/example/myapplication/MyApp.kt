package com.example.myapplication

import android.app.Application
import androidx.room.Room
import com.example.myapplication.data.local.AppDatabase

/**
 * 应用入口 Application 类
 * 负责初始化全局单例：Room 数据库
 */
class MyApp : Application() {

    companion object {
        lateinit var instance: MyApp
            private set

        /** 全局 Room 数据库单例 */
        lateinit var database: AppDatabase
            private set
    }

    override fun onCreate() {
        super.onCreate()
        instance = this

        // 初始化 Room 数据库
        database = Room.databaseBuilder(
            applicationContext,
            AppDatabase::class.java,
            AppDatabase.DATABASE_NAME
        )
            // 开发期间允许主线程查询，方便调试
            // 生产环境可移除
            .allowMainThreadQueries()
            .fallbackToDestructiveMigration()
            .build()
    }
}
