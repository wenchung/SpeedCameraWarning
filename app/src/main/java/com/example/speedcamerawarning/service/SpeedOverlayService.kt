package com.example.speedcamerawarning.service

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.graphics.PixelFormat
import android.os.Build
import android.os.IBinder
import android.view.Gravity
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.WindowManager
import android.widget.ImageButton
import android.widget.TextView
import androidx.cardview.widget.CardView
import androidx.core.app.NotificationCompat
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import com.example.speedcamerawarning.R
import com.example.speedcamerawarning.presentation.main.MainActivity
import kotlin.math.roundToInt

/**
 * 懸浮視窗速度顯示服務
 * 
 * 功能：
 * 1. 顯示即時車速
 * 2. 根據超速程度改變背景顏色：
 *    - 白色：正常（未超速）
 *    - 藍色：輕微超速（速限 +0~10 km/h）
 *    - 黃色：中度超速（速限 +10~20 km/h）
 *    - 紅色：嚴重超速（速限 +20 km/h 以上）
 * 3. 可拖曳移動位置
 * 4. 長按顯示關閉按鈕
 */
class SpeedOverlayService : Service() {

    companion object {
        private const val NOTIFICATION_ID = 9002
        private const val CHANNEL_ID = "speed_overlay_channel"
        
        const val ACTION_UPDATE_SPEED = "com.example.speedcamerawarning.UPDATE_SPEED"
        const val EXTRA_CURRENT_SPEED = "current_speed"
        const val EXTRA_SPEED_LIMIT = "speed_limit"
        
        fun start(context: Context) {
            val intent = Intent(context, SpeedOverlayService::class.java)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                context.startForegroundService(intent)
            } else {
                context.startService(intent)
            }
        }
        
        fun stop(context: Context) {
            context.stopService(Intent(context, SpeedOverlayService::class.java))
        }
        
        fun updateSpeed(context: Context, currentSpeed: Float, speedLimit: Int?) {
            val intent = Intent(ACTION_UPDATE_SPEED).apply {
                putExtra(EXTRA_CURRENT_SPEED, currentSpeed)
                putExtra(EXTRA_SPEED_LIMIT, speedLimit ?: 0)
            }
            LocalBroadcastManager.getInstance(context).sendBroadcast(intent)
        }
    }

    private var windowManager: WindowManager? = null
    private var overlayView: View? = null
    
    // UI 元件
    private var speedCard: CardView? = null
    private var tvCurrentSpeed: TextView? = null
    private var tvSpeedUnit: TextView? = null
    private var tvSpeedLimit: TextView? = null
    private var btnCloseOverlay: ImageButton? = null
    
    // 拖曳相關
    private var initialX = 0
    private var initialY = 0
    private var initialTouchX = 0f
    private var initialTouchY = 0f
    private var lastTouchTime = 0L
    
    // 速度資料
    private var currentSpeed = 0f
    private var currentSpeedLimit: Int? = null

    override fun onCreate() {
        super.onCreate()
        createNotificationChannel()
        createOverlayWindow()
        
        // 註冊廣播接收器接收速度更新
        val filter = android.content.IntentFilter(ACTION_UPDATE_SPEED)
        LocalBroadcastManager.getInstance(this).registerReceiver(
            object : android.content.BroadcastReceiver() {
                override fun onReceive(context: Context?, intent: Intent?) {
                    intent?.let {
                        val speed = it.getFloatExtra(EXTRA_CURRENT_SPEED, 0f)
                        val limit = it.getIntExtra(EXTRA_SPEED_LIMIT, 0)
                        updateSpeedDisplay(speed, if (limit > 0) limit else null)
                    }
                }
            },
            filter
        )
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        startForeground(NOTIFICATION_ID, createNotification())
        return START_STICKY
    }

    override fun onBind(intent: Intent?): IBinder? = null

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                "速度顯示",
                NotificationManager.IMPORTANCE_LOW
            ).apply {
                description = "懸浮視窗速度顯示服務"
                setShowBadge(false)
            }
            
            val notificationManager = getSystemService(NotificationManager::class.java)
            notificationManager.createNotificationChannel(channel)
        }
    }

    private fun createNotification(): android.app.Notification {
        val notificationIntent = Intent(this, MainActivity::class.java)
        val pendingIntent = PendingIntent.getActivity(
            this,
            0,
            notificationIntent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )

        return NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle("速度顯示中")
            .setContentText("懸浮視窗顯示即時車速")
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setContentIntent(pendingIntent)
            .setOngoing(true)
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .build()
    }

    private fun createOverlayWindow() {
        windowManager = getSystemService(WINDOW_SERVICE) as WindowManager
        
        // 載入佈局
        overlayView = LayoutInflater.from(this).inflate(
            R.layout.overlay_speed_widget,
            null
        )
        
        // 綁定 UI 元件
        speedCard = overlayView?.findViewById(R.id.speedCard)
        tvCurrentSpeed = overlayView?.findViewById(R.id.tvCurrentSpeed)
        tvSpeedUnit = overlayView?.findViewById(R.id.tvSpeedUnit)
        tvSpeedLimit = overlayView?.findViewById(R.id.tvSpeedLimit)
        btnCloseOverlay = overlayView?.findViewById(R.id.btnCloseOverlay)
        
        // 設定視窗參數
        val layoutType = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY
        } else {
            @Suppress("DEPRECATION")
            WindowManager.LayoutParams.TYPE_PHONE
        }
        
        val params = WindowManager.LayoutParams(
            WindowManager.LayoutParams.WRAP_CONTENT,
            WindowManager.LayoutParams.WRAP_CONTENT,
            layoutType,
            WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE or
                    WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL or
                    WindowManager.LayoutParams.FLAG_WATCH_OUTSIDE_TOUCH,
            PixelFormat.TRANSLUCENT
        ).apply {
            gravity = Gravity.TOP or Gravity.START
            x = 100
            y = 100
        }
        
        // 設定觸控監聽（拖曳功能）
        setupTouchListener(params)
        
        // 設定關閉按鈕
        btnCloseOverlay?.setOnClickListener {
            stopSelf()
        }
        
        // 將視窗加入
        try {
            windowManager?.addView(overlayView, params)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun setupTouchListener(params: WindowManager.LayoutParams) {
        overlayView?.setOnTouchListener { _, event ->
            when (event.action) {
                MotionEvent.ACTION_DOWN -> {
                    initialX = params.x
                    initialY = params.y
                    initialTouchX = event.rawX
                    initialTouchY = event.rawY
                    lastTouchTime = System.currentTimeMillis()
                    true
                }
                
                MotionEvent.ACTION_MOVE -> {
                    val deltaX = event.rawX - initialTouchX
                    val deltaY = event.rawY - initialTouchY
                    
                    params.x = initialX + deltaX.toInt()
                    params.y = initialY + deltaY.toInt()
                    
                    windowManager?.updateViewLayout(overlayView, params)
                    true
                }
                
                MotionEvent.ACTION_UP -> {
                    val touchDuration = System.currentTimeMillis() - lastTouchTime
                    val deltaX = event.rawX - initialTouchX
                    val deltaY = event.rawY - initialTouchY
                    val distance = kotlin.math.sqrt(deltaX * deltaX + deltaY * deltaY)
                    
                    // 長按顯示關閉按鈕
                    if (touchDuration > 1000 && distance < 10) {
                        btnCloseOverlay?.visibility = View.VISIBLE
                        btnCloseOverlay?.animate()?.alpha(1f)?.setDuration(200)?.start()
                        
                        // 3秒後自動隱藏
                        btnCloseOverlay?.postDelayed({
                            btnCloseOverlay?.animate()?.alpha(0f)?.setDuration(200)
                                ?.withEndAction {
                                    btnCloseOverlay?.visibility = View.GONE
                                }?.start()
                        }, 3000)
                    }
                    true
                }
                
                else -> false
            }
        }
    }

    /**
     * 更新速度顯示和背景顏色
     */
    private fun updateSpeedDisplay(speed: Float, speedLimit: Int?) {
        currentSpeed = speed
        currentSpeedLimit = speedLimit
        
        val speedKmh = speed.roundToInt()
        
        // 更新速度文字
        tvCurrentSpeed?.text = if (speedKmh > 0) speedKmh.toString() else "--"
        
        // 更新速限顯示
        if (speedLimit != null && speedLimit > 0) {
            tvSpeedLimit?.text = "限速 $speedLimit"
            tvSpeedLimit?.visibility = View.VISIBLE
        } else {
            tvSpeedLimit?.visibility = View.GONE
        }
        
        // 根據超速程度改變背景顏色
        val backgroundColor = calculateBackgroundColor(speedKmh, speedLimit)
        val textColor = if (backgroundColor == Color.WHITE) {
            Color.BLACK
        } else {
            Color.WHITE
        }
        
        speedCard?.setCardBackgroundColor(backgroundColor)
        tvCurrentSpeed?.setTextColor(textColor)
        tvSpeedUnit?.setTextColor(textColor)
        tvSpeedLimit?.setTextColor(textColor)
    }

    /**
     * 計算背景顏色
     * 
     * @param currentSpeed 當前車速 (km/h)
     * @param speedLimit 速限 (km/h)
     * @return 背景顏色
     */
    private fun calculateBackgroundColor(currentSpeed: Int, speedLimit: Int?): Int {
        if (speedLimit == null || speedLimit <= 0) {
            // 沒有速限資訊，顯示白色
            return Color.WHITE
        }
        
        val overspeed = currentSpeed - speedLimit
        
        return when {
            overspeed <= 0 -> {
                // 未超速：白色
                Color.WHITE
            }
            overspeed in 1..10 -> {
                // 輕微超速 (+0~10 km/h)：藍色
                Color.parseColor("#2196F3") // Material Blue
            }
            overspeed in 11..20 -> {
                // 中度超速 (+10~20 km/h)：黃色
                Color.parseColor("#FFC107") // Material Amber
            }
            else -> {
                // 嚴重超速 (+20 km/h 以上)：紅色
                Color.parseColor("#F44336") // Material Red
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        
        // 移除懸浮視窗
        overlayView?.let { view ->
            try {
                windowManager?.removeView(view)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
        
        overlayView = null
        windowManager = null
    }
}
