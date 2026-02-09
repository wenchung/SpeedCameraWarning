package com.example.speedcamerawarning.service

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import com.example.speedcamerawarning.R
import com.example.speedcamerawarning.presentation.main.MainActivity
import com.example.speedcamerawarning.util.Constants

/**
 * 通知管理輔助工具
 */
class NotificationHelper(private val context: Context) {
    
    private val notificationManager = 
        context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
    
    init {
        createNotificationChannels()
    }
    
    /**
     * 建立通知頻道（Android 8.0+）
     */
    private fun createNotificationChannels() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            // 前景服務通知頻道
            val serviceChannel = NotificationChannel(
                Constants.NOTIFICATION_CHANNEL_ID,
                Constants.NOTIFICATION_CHANNEL_NAME,
                NotificationManager.IMPORTANCE_LOW
            ).apply {
                description = "測速照相監控服務運行狀態"
                setShowBadge(false)
            }
            
            // 警告通知頻道
            val warningChannel = NotificationChannel(
                "${Constants.NOTIFICATION_CHANNEL_ID}_warning",
                "測速照相警告",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "接近測速照相點時的警告通知"
                enableVibration(true)
                enableLights(true)
            }
            
            notificationManager.createNotificationChannel(serviceChannel)
            notificationManager.createNotificationChannel(warningChannel)
        }
    }
    
    /**
     * 建立前景服務通知
     */
    fun createForegroundNotification(): Notification {
        val intent = Intent(context, MainActivity::class.java)
        val pendingIntent = PendingIntent.getActivity(
            context,
            0,
            intent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )
        
        return NotificationCompat.Builder(context, Constants.NOTIFICATION_CHANNEL_ID)
            .setContentTitle("測速照相監控中")
            .setContentText("正在監控附近的測速照相點")
            .setSmallIcon(R.drawable.ic_speed_camera) // 需要建立圖示
            .setContentIntent(pendingIntent)
            .setOngoing(true)
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .setCategory(NotificationCompat.CATEGORY_SERVICE)
            .build()
    }
    
    /**
     * 更新前景服務通知內容
     */
    fun updateForegroundNotification(
        nearestCameraDistance: String?,
        nearestCameraAddress: String?
    ) {
        val intent = Intent(context, MainActivity::class.java)
        val pendingIntent = PendingIntent.getActivity(
            context,
            0,
            intent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )
        
        val contentText = if (nearestCameraDistance != null && nearestCameraAddress != null) {
            "最近測速點：$nearestCameraAddress ($nearestCameraDistance)"
        } else {
            "正在監控附近的測速照相點"
        }
        
        val notification = NotificationCompat.Builder(context, Constants.NOTIFICATION_CHANNEL_ID)
            .setContentTitle("測速照相監控中")
            .setContentText(contentText)
            .setSmallIcon(R.drawable.ic_speed_camera)
            .setContentIntent(pendingIntent)
            .setOngoing(true)
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .setCategory(NotificationCompat.CATEGORY_SERVICE)
            .build()
        
        notificationManager.notify(Constants.FOREGROUND_SERVICE_NOTIFICATION_ID, notification)
    }
    
    /**
     * 顯示測速照相警告通知
     */
    fun showWarningNotification(
        address: String,
        distance: String,
        speedLimit: Int,
        level: WarningLevel
    ) {
        val intent = Intent(context, MainActivity::class.java)
        val pendingIntent = PendingIntent.getActivity(
            context,
            0,
            intent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )
        
        val (title, priority, icon) = when (level) {
            WarningLevel.CRITICAL -> Triple(
                "⚠️ 緊急警告！",
                NotificationCompat.PRIORITY_MAX,
                R.drawable.ic_warning_critical
            )
            WarningLevel.WARNING -> Triple(
                "⚠️ 測速照相警告",
                NotificationCompat.PRIORITY_HIGH,
                R.drawable.ic_warning
            )
            WarningLevel.NOTICE -> Triple(
                "ℹ️ 測速照相提示",
                NotificationCompat.PRIORITY_DEFAULT,
                R.drawable.ic_info
            )
        }
        
        val notification = NotificationCompat.Builder(
            context,
            "${Constants.NOTIFICATION_CHANNEL_ID}_warning"
        )
            .setContentTitle(title)
            .setContentText("前方 $distance 有測速照相，速限 ${speedLimit}km/h")
            .setStyle(NotificationCompat.BigTextStyle()
                .bigText("地點：$address\n距離：$distance\n速限：${speedLimit}km/h"))
            .setSmallIcon(icon)
            .setContentIntent(pendingIntent)
            .setPriority(priority)
            .setCategory(NotificationCompat.CATEGORY_ALARM)
            .setAutoCancel(true)
            .setVibrate(if (level == WarningLevel.CRITICAL) {
                longArrayOf(0, 500, 200, 500)
            } else {
                longArrayOf(0, 300)
            })
            .build()
        
        notificationManager.notify(Constants.WARNING_NOTIFICATION_ID, notification)
    }
    
    /**
     * 取消警告通知
     */
    fun cancelWarningNotification() {
        notificationManager.cancel(Constants.WARNING_NOTIFICATION_ID)
    }
}

/**
 * 警告等級
 */
enum class WarningLevel {
    CRITICAL,  // 緊急
    WARNING,   // 警告
    NOTICE     // 提示
}
