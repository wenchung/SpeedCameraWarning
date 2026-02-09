package com.example.speedcamerawarning

import android.app.Application
import dagger.hilt.android.HiltAndroidApp

/**
 * Application 類別
 * 使用 Hilt 進行依賴注入
 */
@HiltAndroidApp
class SpeedCameraApp : Application() {
    
    override fun onCreate() {
        super.onCreate()
        // 可以在這裡進行全局初始化
    }
}
