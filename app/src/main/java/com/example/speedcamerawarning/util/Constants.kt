package com.example.speedcamerawarning.util

/**
 * 應用程式常數定義
 */
object Constants {
    
    // 距離閘值（公尺）
    const val DISTANCE_CRITICAL = 300.0  // 緊急警告距離
    const val DISTANCE_WARNING = 500.0   // 警告距離
    const val DISTANCE_NOTICE = 1000.0   // 提示距離
    const val SEARCH_RADIUS_KM = 2.0     // 搜尋半徑（公里）
    
    // 定位更新間隔（毫秒）
    const val LOCATION_UPDATE_INTERVAL_FAST = 2000L      // 高速模式：2秒
    const val LOCATION_UPDATE_INTERVAL_NORMAL = 5000L    // 正常模式：5秒
    const val LOCATION_UPDATE_INTERVAL_SLOW = 10000L     // 省電模式：10秒
    
    // 速度閘值（km/h）
    const val SPEED_THRESHOLD_SLOW = 10.0f   // 慢速閘值
    const val SPEED_THRESHOLD_NORMAL = 40.0f // 正常速度閘值
    
    // Notification
    const val NOTIFICATION_CHANNEL_ID = "speed_camera_warning"
    const val NOTIFICATION_CHANNEL_NAME = "測速照相警告"
    const val FOREGROUND_SERVICE_NOTIFICATION_ID = 1001
    const val WARNING_NOTIFICATION_ID = 1002
    
    // Preferences Keys
    const val PREF_NAME = "speed_camera_prefs"
    const val PREF_WARNING_DISTANCE = "warning_distance"
    const val PREF_VOICE_ENABLED = "voice_enabled"
    const val PREF_VIBRATE_ENABLED = "vibrate_enabled"
    const val PREF_AUTO_UPDATE = "auto_update"
    const val PREF_LAST_UPDATE_TIME = "last_update_time"
    
    // Default Settings
    const val DEFAULT_WARNING_DISTANCE = 500
    const val DEFAULT_VOICE_ENABLED = true
    const val DEFAULT_VIBRATE_ENABLED = true
    const val DEFAULT_AUTO_UPDATE = true
    
    // Data Update
    const val UPDATE_INTERVAL_DAYS = 7L  // 每週自動更新
    
    // Intent Actions
    const val ACTION_START_TRACKING = "com.example.speedcamerawarning.START_TRACKING"
    const val ACTION_STOP_TRACKING = "com.example.speedcamerawarning.STOP_TRACKING"
    
    // WorkManager
    const val WORK_UPDATE_DATA = "update_speed_camera_data"
}
